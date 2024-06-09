import ShipNotification from "../model/ShipNotification";
import HttpSender from "../utils/communication/HttpSender";
import ErrorNotificationService from "./ErrorNotificationService";
import NotificationResponseItem from "../templates/NotificationResponseItem";
import ShipDetails from "../model/ShipDetails";

export class NotificationService {
  // THIS IS ONLY USED FOR OPTIMIZATION REASONS FOR areAllRead() METHOD WHICH
  // THEN DOES NOT HAVE TO QUERY THE BACKEND AGAIN
  private static notifications: ShipNotification[] = [];

  static allNotificationsEndpoint = "/notifications";
  static markNotificationAsReadEndpoint = "/notifications/read/";

  /**
   * Method that queries the backend to mark a single notification as read.
   *
   * @param notification notification object
   */
  static queryBackendToMarkANotificationAsRead = async (
    notification: ShipNotification,
  ) => {
    if (notification.isRead) return;
    await HttpSender.put(
      NotificationService.markNotificationAsReadEndpoint + notification.id,
    );
  };

  /**
   * Method that queries the backend to mark a list of notifications as read.
   * This is called after pressing the read all button.
   *
   * @param notifications a list of notifications that should be marked as read
   */
  static queryBackendToMarkAllNotificationsAsRead = async (
    notifications: ShipNotification[],
  ) => {
    for (let i = 0; i < notifications.length; i++) {
      await NotificationService.queryBackendToMarkANotificationAsRead(
        notifications[i],
      );
    }
  };

  /**
   * Method that fetches all notifications for a particular ship. It is however, for now, not used,
   * as currently we store all notifications in the frontend. However, in the future it may be needed,
   * as we may not want to fetch all the notifications in the history.
   *
   * @param shipID id of the ship for which notifications are
   */
  static queryBackendForAllNotificationsForShip: (
    shipID: number,
  ) => Promise<ShipNotification[]> = async (shipID) => {
    if (shipID < 0) return;

    return await HttpSender.get(
      NotificationService.allNotificationsEndpoint + "/" + shipID,
    );
  };

  /**
   * Method that fetches all notifications from the backend.
   */
  static queryBackendForAllNotifications: () => Promise<ShipNotification[]> =
    async () => {
      const response = await HttpSender.get(
        NotificationService.allNotificationsEndpoint,
      );

      if (!Array.isArray(response)) {
        ErrorNotificationService.addError("Server returned not an array");
        return [];
      }

      const responseWithoutNulls = response.filter((item) => item !== null);
      if (responseWithoutNulls.length !== response.length) {
        ErrorNotificationService.addError(
          "Notifications array contained null items",
        );
      }

      this.notifications = NotificationService.sortList(
        responseWithoutNulls.map(
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          (item: any) => NotificationService.extractNotificationDetails(item),
        ),
        "desc",
      );

      return this.notifications;
    };

  /**
   * Utility method that parses the received JSON data and assembles the
   * corresponding ShipNotifications object
   *
   * @param item the received JSON object
   * @return corresponding ShipNotifications objects
   */
  static extractNotificationDetails: (
    item: NotificationResponseItem,
  ) => ShipNotification = (item) => {
    return new ShipNotification(
      item.id,
      item.isRead,
      new ShipDetails(
        item.shipID,
        item.currentShipDetails.currentAISSignal.heading,
        item.currentShipDetails.currentAISSignal.latitude,
        item.currentShipDetails.currentAISSignal.longitude,
        item.currentShipDetails.currentAISSignal.timestamp,
        item.currentShipDetails.currentAnomalyInformation.score,
        item.currentShipDetails.currentAnomalyInformation.explanation,
        item.currentShipDetails.maxAnomalyScoreInfo.maxAnomalyScore,
        item.currentShipDetails.maxAnomalyScoreInfo.correspondingTimestamp,
        item.currentShipDetails.currentAISSignal.departurePort,
        item.currentShipDetails.currentAISSignal.course,
        item.currentShipDetails.currentAISSignal.speed,
      ),
    );
  };

  /**
   * Utility method that sorts the list of Notifications entries based on their ID (TODO: later change to be sorted by date)
   *
   * @param list - fetched list of notifications
   * @param order - either `asc` for ascending or '`desc` for descending (default)
   */
  static sortList: (
    list: ShipNotification[],
    order: string,
  ) => ShipNotification[] = (list, order = "desc") => {
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

  // equality is based on array length and notification ids
  static notificationArraysEqual(
    notifications1: ShipNotification[],
    notifications2: ShipNotification[],
  ) {
    if (notifications1.length !== notifications2.length) {
      return false;
    }

    for (let i = 0; i < notifications1.length; i++) {
      const not1 = notifications1[i];
      const not2 = notifications2[i];

      if (not1.id !== not2.id) {
        return false;
      }
    }

    return true;
  }
}
