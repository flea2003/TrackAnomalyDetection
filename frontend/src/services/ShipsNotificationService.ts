import React from "react";
import ShipNotification from "../model/ShipNotification";
import HttpSender from "../utils/HttpSender";
import ErrorNotificationService, { ErrorNotification, ErrorSeverity } from "./ErrorNotificationService";
import APIResponseItem from "../templates/APIResponseItem";
import NotificationResponseItem from "../templates/NotificationResponseItem";
import errorSymbol from "../assets/icons/error-notifications/error.svg";
import warningSymbol from "../assets/icons/error-notifications/warning.svg";
import infoSymbol from "../assets/icons/error-notifications/info.svg";
import notificationResponseItem from "../templates/NotificationResponseItem";


export class ShipsNotificationService {
  // THIS IS ONLY USED FOR OPTIMIZATION REASONS FOR areAllRead() METHOD WHICH
  // THEN DOES NOT HAVE TO QUERY THE BACKEND AGAIN
  private static notifications: ShipNotification[] = [];

  static allNotificationsEndpoint = "/notifications";
  static markAsReadEndpoint = "/notifications/read/";

  static refreshState = () => {
    console.log("ShipNotificationService refreshState was not set up");
  };

  static queryBackendToMarkANotificationAsRead = async (details: ShipNotification) => {
    if (details.isRead) return;
    await HttpSender.put(ShipsNotificationService.markAsReadEndpoint + details.id);
  }

  static queryBackendToMarkAllNotificationsAsRead = async (notifications: ShipNotification[]) => {
    for (let i = 0; i < notifications.length; i++) {
        await ShipsNotificationService.queryBackendToMarkANotificationAsRead(notifications[i]);
    }
  }

  static queryBackendForAllNotificationsForShip: (shipID: number) => Promise<ShipNotification[]> = async (shipID) => {
      return await HttpSender.get(ShipsNotificationService.allNotificationsEndpoint + "/" + shipID);
  }

  static queryBackendForAllNotifications: () => Promise<ShipNotification[]> = async () => {
    const response = await HttpSender.get(
      ShipsNotificationService.allNotificationsEndpoint,
    );

    if (!Array.isArray(response)) {
      ErrorNotificationService.addError("Server returned not an array");
      return [];
    }

    if (response.length === 0) {
      ErrorNotificationService.addInformation("Ship array is empty");
      return [];
    }

    const responseWithoutNulls = response.filter((item) => item !== null);
    if (responseWithoutNulls.length !== response.length) {
      ErrorNotificationService.addError("Notifications array contained null items");
    }

    this.notifications = ShipsNotificationService.sortList(
      responseWithoutNulls.map(
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (item: any) => ShipsNotificationService.extractNotificationDetails(item),
      ),
      "desc",
    );

    return this.notifications;
  }
  /**
   * Utility method that parses the received JSON data and assembles the
   * corresponding ShipDetails object
   * @param item - the received JSON object
   * @return - dummy ShipDetails object
   */
  static extractNotificationDetails: (item: NotificationResponseItem) => ShipNotification = (
    item,
  ) => {
    console.log(item)
    return new ShipNotification(
        item.id,
        item.shipID,
        item.read,
        item.currentShipDetails.currentAISSignal.heading,
        item.currentShipDetails.currentAISSignal.latitude,
        item.currentShipDetails.currentAISSignal.longitude,
        item.currentShipDetails.currentAnomalyInformation.score,
        item.currentShipDetails.currentAnomalyInformation.explanation,
        item.currentShipDetails.maxAnomalyScoreInfo.maxAnomalyScore,
        item.currentShipDetails.maxAnomalyScoreInfo.correspondingTimestamp,
        item.currentShipDetails.currentAISSignal.departurePort,
        item.currentShipDetails.currentAISSignal.course,
        item.currentShipDetails.currentAISSignal.speed
      );
  };

  /**
   * Utility method that sorts the list of ShipDetails entries based on the corresponding values of the anomaly score.
   * @param list - fetched list with ShipDetails instances
   * @param order - either `asc` for ascending or '`desc` for descending (default)
   */
  static sortList: (list: ShipNotification[], order: string) => ShipNotification[] = (
    list,
    order = "desc",
  ) => {
    if (!["desc", "asc"].includes(order)) {
      ErrorNotificationService.addError("Invalid sorting order");
      return [];
    }
    const sortedList = list.sort((a, b) => {
      const aScore = a.id;
      const bScore = b.id;
      if (aScore > bScore) {
        return -1;
      }
      if (aScore === bScore) {
        return 0;
      } else {
        return 1;
      }
    });
    if (order === "asc") {
      return sortedList.reverse();
    }
    return sortedList;
  };

  /**
   * Checks if all current notifications are marked as read.
   */
  static areAllRead() {
    return this.notifications.every((notification) => notification.isRead);
  }
}

export default ShipNotification;
