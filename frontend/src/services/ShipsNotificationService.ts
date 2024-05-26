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
  static notificationsEndpoint = "/notifications"

  static refreshState = () => {
    console.log("ShipNotificationService refreshState was not set up");
  };

  static queryBackendForNotificationsArray: () => Promise<ShipNotification[]> = async () => {
    const response = await HttpSender.get(
      ShipsNotificationService.notificationsEndpoint,
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
    // eslint-disable-next-line @typescript-eslint/no-unused-expressions
    ShipNotification

    return new ShipNotification(
        item.id,
        item.shipID,
        0,
        0,
        0,
        item.currentShipDetails.currentAnomalyInformation.score,
        item.currentShipDetails.currentAnomalyInformation.explanation,
        item.currentShipDetails.maxAnomalyScoreInfo.maxAnomalyScore,
        item.currentShipDetails.maxAnomalyScoreInfo.correspondingTimestamp,
        "CIUJU KAD PAEJO",
        0,
        0,
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
      const aScore = a.getAnomalyScore();
      const bScore = b.getAnomalyScore();
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
