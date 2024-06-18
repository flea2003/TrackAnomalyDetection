import ShipNotification from "../model/ShipNotification";
import HttpSender from "../utils/communication/HttpSender";
import ErrorNotificationService from "./ErrorNotificationService";
import NotificationResponseItem from "../templates/NotificationResponseItem";
import ShipDetails from "../model/ShipDetails";
import TimeUtilities from "../utils/TimeUtilities";
import endpointConfig from "../configs/endpointsConfig.json";
import Cookies from "js-cookie";

export class NotificationService {
  // Stores ids of notifications that have been read by the user since the page was refreshed
  static idsOfReadNotifications: number[] = [];

  // Endpoint for accessing all notifications
  static allNotificationsEndpoint = endpointConfig["allNotificationsEndpoint"];

  // Endpoint for accessing all notifications for a particular ship
  static getNotificationWithIdEndpoint =
    endpointConfig["getNotificationWithIdEndpoint"];

  /**
   * Method that initializes the array of idsOfReadNotifications. It takes all notifications
   * that are in the backend before the frontend has started, and marks them as read.
   */
  static initializeReadNotifications = async () => {
    const cookieValue = Cookies.get("idsOfReadNotifications");
    if (cookieValue === undefined) {
      // Fetch all notifications that are currently in the backend
      const allNotifications =
        await NotificationService.queryBackendForAllNotifications();
      NotificationService.markAllNotificationsAsRead(allNotifications);

      const jsonIds = JSON.stringify(allNotifications.map((x) => x.id));
      Cookies.set("idsOfReadNotifications", jsonIds, { expires: 7 });
    } else {
      this.idsOfReadNotifications = JSON.parse(cookieValue);
    }
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
   * Method that fetches all notifications for a particular ship. Unlike the
   * history of all notifications, this endpoint does not limit the amount of
   * notifications that are fetched from backend. Note that this method, once
   * fetched, assigns proper isRead values to the notifications
   *
   * @param shipID id of the ship for which notifications are fetched
   */
  static getAllNotificationsForShip: (
    shipID: number,
  ) => Promise<ShipNotification[]> = async (shipID) => {
    if (shipID < 0) {
      ErrorNotificationService.addWarning("Notification ID was negative");
      return [];
    } else {
      const newNotifications: ShipNotification[] =
        await this.queryBackendForAllNotificationsForShip(shipID);

      return newNotifications.map((notification) => {
        if (this.idsOfReadNotifications.includes(notification.id)) {
          notification.isRead = true;
        }
        return notification;
      });
    }
  };

  /**
   * Function that fetches all notifications for a particular ship. Unlike the
   * history of all notifications, this function does not limit the amount of
   * notifications that are fetched from backend. Note that this method only
   * queries the backend, but does not deal with assigning proper isRead value
   *
   * @param shipID id of the ship for which notifications are fetched
   */
  static queryBackendForAllNotificationsForShip: (
    shipID: number,
  ) => Promise<ShipNotification[]> = async (shipID) => {
    // Check if the ID is valid
    if (shipID < 0) {
      ErrorNotificationService.addWarning("Notification ID was negative");
      return [];
    } else
      return NotificationService.queryBackendHttpGet(
        NotificationService.getNotificationWithIdEndpoint + shipID,
      );
  };

  /**
   * Function that, given an array of notifications fetched straight from backend,
   * assigns needed  isRead status. Those notifications that have been read by
   * the user (meaning that their ID is in idsOfReadNotifications array) are marked
   * as read, otherwise - left at false.
   *
   * @param newNotifications
   */
  static updateNotifications(newNotifications: ShipNotification[]) {
    return newNotifications.map((x) => {
      if (this.idsOfReadNotifications.includes(x.id)) {
        x.isRead = true;
        return x;
      } else {
        return x;
      }
    });
  }

  /**
   * Function that checks if all notifications in a list are marked as read.
   *
   * @param notifications array of ShipNotification objects
   */
  static areAllRead(notifications: ShipNotification[]) {
    return notifications.every((notification) => notification.isRead);
  }

  /**
   * Method that marks a list of notifications as read. It does not query the backend,
   * as currently notifications are sent locally. This method is called after
   * pressing the read all button.
   *
   * @param notifications an of notifications that will be marked as read
   */
  static markAllNotificationsAsRead = (notifications: ShipNotification[]) => {
    for (let i = 0; i < notifications.length; i++) {
      this.markANotificationAsRead(notifications[i]);
    }
  };

  /**
   * Method that marks a single notification as read. It does not query the backend,
   * as currently notification read status is stored locally. This method is called after
   * clicking on a notification button
   *
   * @param notification notification object
   */
  static markANotificationAsRead = (notification: ShipNotification) => {
    if (notification.isRead) {
      return;
    }

    notification.isRead = true;
    this.idsOfReadNotifications.push(notification.id);

    const updatedArrayJson = JSON.stringify(this.idsOfReadNotifications);
    Cookies.set("idsOfReadNotifications", updatedArrayJson, { expires: 7 });
  };

  /**
   * Utility method that sorts the list of Notifications entries based on their date.
   * Newest notifications are put in front.
   *
   * @param list fetched list of notifications
   * @param order order in which notifications are sorted.
   * Either `asc` for ascending or '`desc` for descending (default)
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
   * Function that fetches a list of notifications from backend, depending on
   * needed endpoint. It is extracted so less code duplication exists.
   *
   * @param endpoint name of the endpoint that is being requested
   */
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
      false, // Set notification status as NOT read
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
}
