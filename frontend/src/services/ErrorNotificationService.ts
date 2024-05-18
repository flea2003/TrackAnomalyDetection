import errorSymbol from '../assets/icons/error-notifications/error.svg'
import warningSymbol from '../assets/icons/error-notifications/warning.svg'
import infoSymbol from '../assets/icons/error-notifications/info.svg'

export enum ErrorSeverity {
  ERROR = "error",
  WARNING = "warning",
  INFO = "information"
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

class ErrorNotificationService {
  private static notifications: ErrorNotification[] = []

  static addError(message: string) {
    this.addNotification(new ErrorNotification(message, ErrorSeverity.ERROR));
  }

  static addWarning(message: string) {
    this.addNotification(new ErrorNotification(message, ErrorSeverity.WARNING));
  }

  static addInformation(message: string) {
    this.addNotification(new ErrorNotification(message, ErrorSeverity.INFO));
  }

  private static addNotification(notification: ErrorNotification) {
    this.notifications.push(notification);
  }

  static getAllNotifications() {
    return this.notifications;
  }

  static markAllAsRead() {
    this.notifications.forEach((notification) => {notification.wasRead = true})
  }

  static areAllRead() {
    return this.notifications.every(notification => notification.wasRead);
  }

  /**
   * Removes the notification(s) with the given id.
   *
   * @param id the id of the notification to remove.
   */
  static removeNotification(id: number) {
    this.notifications = this.notifications.filter(notification => notification.id !== id);
  }

}

export default ErrorNotificationService;