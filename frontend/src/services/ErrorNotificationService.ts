import errorSymbol from "../assets/icons/error-notifications/error.svg";
import warningSymbol from "../assets/icons/error-notifications/warning.svg";
import infoSymbol from "../assets/icons/error-notifications/info.svg";
import React from "react";

export enum ErrorSeverity {
  ERROR = "error",
  WARNING = "warning",
  INFO = "information",
}

export class ErrorNotification {
  private static idCounter = 0; // used for assigning IDs

  readonly id: number;
  readonly message: string;
  readonly severity: ErrorSeverity;
  wasRead: boolean;

  constructor(message: string, severity: ErrorSeverity, wasRead = false) {
    this.id = ErrorNotification.idCounter++;
    this.message = message;
    this.severity = severity;
    this.wasRead = wasRead;
  }

  /**
   * Returns the corresponding icon based on the severity.
   * The returned icon can then be used in the `src` field of `<img>` elements.
   */
  getIcon() {
    switch (this.severity) {
      case ErrorSeverity.ERROR:
        return errorSymbol;
      case ErrorSeverity.WARNING:
        return warningSymbol;
      case ErrorSeverity.INFO:
        return infoSymbol;
    }
  }
}

/**
 * Service responsible for handling the errors coming from software and
 * error handling in our code.
 *
 * This service is then used by `ErrorList` component to show the errors on
 * the side of the map.
 */
class ErrorNotificationService {
  private static notifications: ErrorNotification[] = [];
  private static refreshState = () => {
    console.log("ErrorNotificationService refreshState was not set up");
  };

  static savedNotificationsLimit = 10000; // will not store more notifications than this number
  static returnedNotificationsLimit = 100; // will not return more notifications than this number

  /**
   * Initialize the service by providing the function that should be called to update the state.
   * The state is created in `App.tsx` by calling `useState`, and the setter is passed here.
   *
   * @param setErrorNotificationsService the setter of the state created in the `App.tsx`
   */
  static initialize(
    setErrorNotificationsService: React.Dispatch<
      React.SetStateAction<ErrorNotification[]>
    >,
  ) {
    this.refreshState = () =>
      setErrorNotificationsService(this.getAllNotifications());
  }

  /**
   * Adds an error to current list of notifications.
   *
   * @param message the message of the error.
   */
  static addError(message: string) {
    this.addNotification(new ErrorNotification(message, ErrorSeverity.ERROR));
  }

  /**
   * Adds a warning to current list of notifications.
   *
   * @param message the message of the warning.
   */
  static addWarning(message: string) {
    this.addNotification(new ErrorNotification(message, ErrorSeverity.WARNING));
  }

  /**
   * Adds an information notification to current list of notifications.
   *
   * @param message the message of this information notification.
   */
  static addInformation(message: string) {
    this.addNotification(new ErrorNotification(message, ErrorSeverity.INFO));
  }

  /**
   * Private helper method to add notification to the current list of notifications.
   *
   * @param notification the notification object to add to the list
   * @private
   */
  private static addNotification(notification: ErrorNotification) {
    this.notifications.push(notification);
    this.checkNotificationLimit();

    this.refreshState();
  }

  /**
   * Checks if there are not too many notifications, and if there are,
   * removes the oldest one (the first in the list).
   */
  public static checkNotificationLimit() {
    if (this.notifications.length <= this.savedNotificationsLimit) {
      return; // everything is fine
    }

    // remove the oldest notification
    this.removeNotification(this.notifications[0].id);
  }

  /**
   * Gets all current notifications.
   * If there are more notifications that the returnedNotificationsLimit, then
   * only the most recent ones are returned.
   */
  static getAllNotifications() {
    return this.notifications.slice(-this.returnedNotificationsLimit);
  }

  /**
   * Marks all current notifications as read.
   */
  static markAllAsRead() {
    this.notifications.forEach((notification) => {
      notification.wasRead = true;
    });

    this.refreshState();
  }

  /**
   * Mark the notification with the given id as read.
   *
   * @param id the id of the notification to be marked as read
   */
  static markAsRead(id: number) {
    this.notifications
      .filter((notification) => notification.id === id)
      .forEach((notification) => {
        notification.wasRead = true;
      });

    this.refreshState();
  }

  /**
   * Checks if all current notifications are marked as read.
   */
  static areAllRead() {
    return this.notifications.every((notification) => notification.wasRead);
  }

  /**
   * Removes the notification(s) with the given id.
   *
   * @param id the id of the notification to remove.
   */
  static removeNotification(id: number) {
    this.notifications = this.notifications.filter(
      (notification) => notification.id !== id,
    );

    this.refreshState();
  }

  /**
   * Clears all current notifications.
   *
   * Currently, this is used only for tests.
   */
  static clearAllNotifications() {
    this.notifications = [];

    this.refreshState();
  }
}

export default ErrorNotificationService;
