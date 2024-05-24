import React from "react";
import ShipNotification from "../model/ShipNotification";
import HttpSender from "../utils/HttpSender";
import ErrorNotificationService from "./ErrorNotificationService";
import APIResponseItem from "../templates/APIResponseItem";

export class ShipsNotificationService {
  static notificationsEndpoint = "/notifications"

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
        (item: any) => ShipsNotificationService.extractCurrentShipDetails(item),
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
  static extractCurrentShipDetails: (item: APIResponseItem) => ShipNotification = (
    item,
  ) => {
    if (
      !item.currentAISSignal &&
      item.currentAnomalyInformation &&
      item.maxAnomalyScoreInfo
    ) {
      return new ShipNotification(
        item.currentAnomalyInformation.id,
        0,
        0,
        0,
        item.currentAnomalyInformation.score,
        item.currentAnomalyInformation.explanation,
        item.maxAnomalyScoreInfo.maxAnomalyScore,
        item.maxAnomalyScoreInfo.correspondingTimestamp,
        "Information not available (yet)",
        0,
        0,
      );
    }
    if (
      item.currentAISSignal &&
      (!item.currentAnomalyInformation || !item.maxAnomalyScoreInfo)
    ) {
      return new ShipNotification(
        item.currentAISSignal.id,
        item.currentAISSignal.heading,
        item.currentAISSignal.latitude,
        item.currentAISSignal.longitude,
        -1,
        "Information not available (yet)",
        0,
        "Information not available (yet)",
        item.currentAISSignal.departurePort,
        item.currentAISSignal.course,
        item.currentAISSignal.speed,
      );
    } else {
      return new ShipNotification(
        item.currentAISSignal.id,
        item.currentAISSignal.heading,
        item.currentAISSignal.latitude,
        item.currentAISSignal.longitude,
        item.currentAnomalyInformation.score,
        item.currentAnomalyInformation.explanation,
        item.maxAnomalyScoreInfo.maxAnomalyScore,
        item.maxAnomalyScoreInfo.correspondingTimestamp,
        item.currentAISSignal.departurePort,
        item.currentAISSignal.course,
        item.currentAISSignal.speed,
      );
    }
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
