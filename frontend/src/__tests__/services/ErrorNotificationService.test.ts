import ErrorNotificationService, {
  ErrorNotification,
  ErrorSeverity,
} from "../../services/ErrorNotificationService";

afterEach(() => {
  ErrorNotificationService.clearAllNotifications();
  ErrorNotificationService.savedNotificationsLimit = 10000; // bring back the limit if it was changed
  ErrorNotificationService.returnedNotificationsLimit = 100; // in case it was changed
});

test("Notifications get assigned different IDs", () => {
  const notification1 = new ErrorNotification("msg1", ErrorSeverity.ERROR);
  const notification2 = new ErrorNotification("msg2", ErrorSeverity.ERROR);

  expect(notification1.id).not.toBe(notification2);
});

test("Notification objects return correct icons", () => {
  const error = new ErrorNotification("msg1", ErrorSeverity.ERROR);
  const warning = new ErrorNotification("msg2", ErrorSeverity.WARNING);
  const info = new ErrorNotification("msg3", ErrorSeverity.INFO);

  expect(error.getIcon()).toBe("error.svg");
  expect(warning.getIcon()).toBe("warning.svg");
  expect(info.getIcon()).toBe("info.svg");
});

test("Notifications are added in the order the methods are called", () => {
  ErrorNotificationService.addError("error");
  ErrorNotificationService.addWarning("warning");
  ErrorNotificationService.addInformation("information");

  const allNotifications = ErrorNotificationService.getAllNotifications();

  expect(allNotifications.length).toBe(3);

  // first element should be error
  expect(allNotifications[0].severity).toBe(ErrorSeverity.ERROR);
  expect(allNotifications[0].message).toBe("error");

  // second element should be error
  expect(allNotifications[1].severity).toBe(ErrorSeverity.WARNING);
  expect(allNotifications[1].message).toBe("warning");

  // third element should be error
  expect(allNotifications[2].severity).toBe(ErrorSeverity.INFO);
  expect(allNotifications[2].message).toBe("information");
});

test("areAllRead test", () => {
  ErrorNotificationService.addError("error");
  ErrorNotificationService.addWarning("warning");

  const notifications = ErrorNotificationService.getAllNotifications();
  notifications[0].wasRead = true;

  expect(ErrorNotificationService.areAllRead()).toBe(false);

  notifications[1].wasRead = true;

  expect(ErrorNotificationService.areAllRead()).toBe(true);
});

test("markAllRead test", () => {
  ErrorNotificationService.addError("error");
  ErrorNotificationService.addWarning("warning");

  ErrorNotificationService.markAllAsRead();

  expect(ErrorNotificationService.areAllRead()).toBe(true);

  const notifications = ErrorNotificationService.getAllNotifications();
  expect(notifications[0].wasRead).toBe(true);
  expect(notifications[1].wasRead).toBe(true);
});

test("removeNotification test", () => {
  ErrorNotificationService.addError("error");
  ErrorNotificationService.addWarning("warning");

  let notifications = ErrorNotificationService.getAllNotifications();

  ErrorNotificationService.removeNotification(notifications[0].id);

  notifications = ErrorNotificationService.getAllNotifications();

  expect(notifications.length).toBe(1);
  expect(notifications[0].message).toBe("warning");
});

test("the oldest notification is removed when called checkNotificationLimit", () => {
  ErrorNotificationService.savedNotificationsLimit = 1; // arbitrarily lower the limit

  ErrorNotificationService.addError("error");
  ErrorNotificationService.addWarning("warning");

  const notifications = ErrorNotificationService.getAllNotifications();

  expect(notifications.length).toBe(1);
  expect(notifications[0].message).toBe("warning");
});

test("get all notifications does not return more than the limit", () => {
  ErrorNotificationService.returnedNotificationsLimit = 1; // arbitrarily lower the limit

  ErrorNotificationService.addError("error");
  ErrorNotificationService.addWarning("warning");

  const notifications = ErrorNotificationService.getAllNotifications();

  // should return the one most recent one
  expect(notifications.length).toBe(1);
  expect(notifications[0].message).toBe("warning");
});
