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
  static getAllNotificationsEndpoint = "/notifications";
  static markAsReadEndpoint = "/notifications/read/";

  static refreshState = () => {
    console.log("ShipNotificationService refreshState was not set up");
  };

  static markAsRead = async (details: ShipNotification) => {
    if (details.isRead) return;
    await HttpSender.put(ShipsNotificationService.markAsReadEndpoint + details.id);
  }

  static markAllAsRead = async (notifications: ShipNotification[]) => {
    for (let i = 0; i < notifications.length; i++) {
        await ShipsNotificationService.markAsRead(notifications[i]);
    }
  }

  static queryBackendForNotificationsArray: () => Promise<ShipNotification[]> = async () => {
    const response = await HttpSender.get(
      ShipsNotificationService.getAllNotificationsEndpoint,
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

    return ShipsNotificationService.sortList(
      responseWithoutNulls.map(
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (item: any) => ShipsNotificationService.extractNotificationDetails(item),
      ),
      "desc",
    );

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

}

export default ShipNotification;
