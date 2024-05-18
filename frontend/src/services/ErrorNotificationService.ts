export enum ErrorSeverity {
  ERROR = "error",
  WARNING = "warning",
  INFO = "information"
}

export class ErrorNotification {
  readonly message: string;
  readonly severity: ErrorSeverity;
  wasRead: boolean;

  constructor(message: string, severity: ErrorSeverity, wasRead = false) {
    this.message = message;
    this.severity = severity;
    this.wasRead = wasRead;
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
    this.notifications.every(notification => notification.wasRead);
  }

}

export default ErrorNotificationService;