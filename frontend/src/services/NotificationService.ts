import ShipNotification from "../model/ShipNotification";
import HttpSender from "../utils/communication/HttpSender";
import ErrorNotificationService from "./ErrorNotificationService";
import NotificationResponseItem from "../templates/NotificationResponseItem";
import ShipDetails from "../model/ShipDetails";
import TimeUtilities from "../utils/TimeUtilities";

export class NotificationService {
  // Stores ids of notifications that have been read
  static idsOfReadNotifications: number[] = [];

  // Endpoint for accessing all notifications
  static allNotificationsEndpoint = "/notifications";

  static getNotificationWithIdEndpoint = "/notifications/ship/";

  // Method to initialize idsOfReadNotifications with all fetched notification IDs
  static initializeReadNotifications = async () => {
    const allNotifications =
      await NotificationService.queryBackendForAllNotifications();
    NotificationService.idsOfReadNotifications = allNotifications.map(
      (notification) => notification.id,
    );
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
    if (shipID < 0) {
      ErrorNotificationService.addWarning("Notification ID was negative");
      return [];
    } else
      return NotificationService.queryBackendHttpGet(
        NotificationService.getNotificationWithIdEndpoint + shipID,
      );
  };

  static getAllNotificationsForShip: (
    shipID: number,
  ) => Promise<ShipNotification[]> = async (shipID) => {
    const newNotifications: ShipNotification[] =
      await this.queryBackendForAllNotificationsForShip(shipID);
    return newNotifications.map((notification) => {
      if (this.idsOfReadNotifications.includes(notification.id)) {
        notification.isRead = true;
      }
      return notification;
    });
  };

  /**
   * Method that fetches all notifications from the backend.
   */
  static queryBackendForAllNotifications: () => Promise<ShipNotification[]> =
    async () => {
      return NotificationService.queryBackendHttpGet(
        NotificationService.allNotificationsEndpoint,
      );
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
      false,
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
   * Utility method that sorts the list of Notifications entries based on their date
   *
   * @param list fetched list of notifications
   * @param order either `asc` for ascending or '`desc` for descending (default)
   */
  static sortList = (list: ShipNotification[], order = "desc") => {
    if (!["desc", "asc"].includes(order)) {
      ErrorNotificationService.addError("Invalid sorting order");
      return [];
    }
    const sortedList = list.sort((a, b) => {
      return TimeUtilities.compareDates(
        a.shipDetails.timestamp,
        b.shipDetails.timestamp,
      );
    });
    if (order === "asc") {
      return sortedList.reverse();
    }
    return sortedList;
  };

  /**
   * Checks if all current notifications are marked as read.
   */
  static areAllRead(notifications: ShipNotification[]) {
    return notifications.every((notification) => notification.isRead);
  }

  /**
   *
   * @param newNotifications
   */
  static updateNotifications(newNotifications: ShipNotification[]) {
    return newNotifications.map((x) => {
      if (this.idsOfReadNotifications.includes(x.id)) {
        x.isRead = true;
        return x;
      } else return x;
    });
  }

  static queryBackendHttpGet: (
    endpoint: string,
  ) => Promise<ShipNotification[]> = async (endpoint) => {
    const response = await HttpSender.get(endpoint);

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

    return NotificationService.sortList(
      responseWithoutNulls.map(
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (item: any) => NotificationService.extractNotificationDetails(item),
      ),
      "desc",
    );
  };

  /**
   * Method that marks a single notification as read. It does not query the backend,
   * as currently notifications are sent locally. This method is called after
   * clicking on a notification button
   *
   * @param notification notification object
   */
  static markANotificationAsRead = (notification: ShipNotification) => {
    if (notification.isRead) return;
    notification.isRead = true;
    this.idsOfReadNotifications.push(notification.id);
  };

  /**
   * Method that marks a list of notifications as read. It does not query the backend,
   * as currently notifications are sent locally. This method is called after
   * pressing the read all button.
   *
   */
  static markAllNotificationsAsRead = (notifications: ShipNotification[]) => {
    for (let i = 0; i < notifications.length; i++) {
      this.markANotificationAsRead(notifications[i]);
    }
  };
}
