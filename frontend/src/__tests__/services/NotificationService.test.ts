import HttpSender from "../../utils/communication/HttpSender";
import ShipDetails from "../../model/ShipDetails";
import ErrorNotificationService from "../../services/ErrorNotificationService";
import NotificationResponseItem from "../../templates/NotificationResponseItem";
import { NotificationService } from "../../services/NotificationService";
import ShipNotification from "../../model/ShipNotification";

const fakeNotificationResponseItem1: NotificationResponseItem = {
  id: 0,
  shipID: 1,
  read: false,
  currentShipDetails: {
    currentAISSignal: {
      id: 1,
      speed: 350.0,
      longitude: 29.0,
      latitude: 47.0,
      course: 90,
      heading: 1,
      timestamp: "t1",
      departurePort: "p1",
    },
    currentAnomalyInformation: {
      id: 1,
      score: 1,
      explanation: "explanation",
      correspondingTimestamp: "t1",
    },
    maxAnomalyScoreInfo: {
      maxAnomalyScore: 1,
      correspondingTimestamp: "t1",
    },
  },
};

const fakeNotificationResponseItem2: NotificationResponseItem = {
  id: 1,
  shipID: 1,
  read: false,
  currentShipDetails: {
    currentAISSignal: {
      id: 1,
      speed: 350.0,
      longitude: 29.0,
      latitude: 47.0,
      course: 90,
      heading: 1,
      timestamp: "t1",
      departurePort: "p1",
    },
    currentAnomalyInformation: {
      id: 1,
      score: 1,
      explanation: "explanation",
      correspondingTimestamp: "t1",
    },
    maxAnomalyScoreInfo: {
      maxAnomalyScore: 1,
      correspondingTimestamp: "t1",
    },
  },
};

const resultItem1 = new ShipNotification(
  0,
  false,
  new ShipDetails(
    1,
    1,
    47.0,
    29.0,
    "t1",
    1,
    "explanation",
    1,
    "t1",
    "p1",
    90,
    350.0,
  ),
);

const resultItem2 = new ShipNotification(
  1,
  false,
  new ShipDetails(
    1,
    1,
    47.0,
    29.0,
    "t1",
    1,
    "explanation",
    1,
    "t1",
    "p1",
    90,
    350.0,
  ),
);

jest.mock("../../utils/communication/HttpSender");

beforeEach(() => {
  // Make refreshState do nothing, so that it does not print to console.
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  ErrorNotificationService.refreshState = () => {};
});

test("query-mark-notification-as-read", async () => {
  await NotificationService.queryBackendToMarkANotificationAsRead(resultItem1);
  expect(HttpSender.put).toHaveBeenCalledTimes(1);
  expect(HttpSender.put).toHaveBeenCalledWith(
    NotificationService.markNotificationAsReadEndpoint + "0",
  );
});

test("query-mark-notification-as-read-already-read", async () => {
  resultItem1.isRead = true;
  await NotificationService.queryBackendToMarkANotificationAsRead(resultItem1);
  expect(HttpSender.put).toHaveBeenCalledTimes(0);
  resultItem1.isRead = false;
});

test("query-mark-all-notifications-as-read-proper", async () => {
  await NotificationService.queryBackendToMarkAllNotificationsAsRead([
    resultItem1,
    resultItem2,
  ]);
  expect(HttpSender.put).toHaveBeenCalledTimes(2);
  expect(HttpSender.put).toHaveBeenCalledWith(
    NotificationService.markNotificationAsReadEndpoint + "0",
  );
  expect(HttpSender.put).toHaveBeenCalledWith(
    NotificationService.markNotificationAsReadEndpoint + "1",
  );
});

test("query-mark-all-notifications-as-read-zero", async () => {
  await NotificationService.queryBackendToMarkAllNotificationsAsRead([]);
  expect(HttpSender.put).toHaveBeenCalledTimes(0);
});

test("query-mark-all-notifications-for-a-ship-valid", async () => {
  await NotificationService.queryBackendForAllNotificationsForShip(1);
  expect(HttpSender.get).toHaveBeenCalledTimes(1);
  expect(HttpSender.get).toHaveBeenCalledWith("/notifications/1");
});

test("query-mark-all-notifications-for-a-ship-invalid", async () => {
  await NotificationService.queryBackendForAllNotificationsForShip(-1);
  expect(HttpSender.get).toHaveBeenCalledTimes(0);
});

test("backend-fetching-all-notifications-invalid", async () => {
  HttpSender.get = jest.fn().mockReturnValue(Promise.resolve([null]));
  const result = await NotificationService.queryBackendForAllNotifications();
  expect(result).toStrictEqual([]);
});

test("backend-fetching-all-notifications-empty-array", async () => {
  HttpSender.get = jest.fn().mockReturnValue(Promise.resolve([]));
  const result = await NotificationService.queryBackendForAllNotifications();
  expect(result).toStrictEqual([]);
});

test("backend-fetching-all-notifications-invalid-type", async () => {
  HttpSender.get = jest.fn().mockReturnValue(Promise.resolve("value"));
  const result = await NotificationService.queryBackendForAllNotifications();
  expect(result).toStrictEqual([]);
});

test("backend-fetching-all-notifications-valid-details", async () => {
  HttpSender.get = jest
    .fn()
    .mockReturnValue(
      Promise.resolve([
        fakeNotificationResponseItem1,
        fakeNotificationResponseItem2,
      ]),
    );
  const result = await NotificationService.queryBackendForAllNotifications();
  expect(result).toStrictEqual([resultItem2, resultItem1]);
});

test("backend-fetching-all-notifications-some-invalid-details", async () => {
  HttpSender.get = jest
    .fn()
    .mockReturnValue(
      Promise.resolve([
        fakeNotificationResponseItem1,
        null,
        fakeNotificationResponseItem2,
      ]),
    );
  const result = await NotificationService.queryBackendForAllNotifications();
  expect(result).toStrictEqual([resultItem2, resultItem1]);
});

test("extract-notification-details-valid", async () => {
  const result = NotificationService.extractNotificationDetails(
    fakeNotificationResponseItem1,
  );
  expect(result).toStrictEqual(resultItem1);
});

test("sorting-valid-notification-list", async () => {
  HttpSender.get = jest
    .fn()
    .mockReturnValue(
      Promise.resolve([
        fakeNotificationResponseItem2,
        fakeNotificationResponseItem1,
      ]),
    );
  const result = await NotificationService.queryBackendForAllNotifications();
  expect(result).toStrictEqual([resultItem2, resultItem1]); // Order here is fine!
});

test("sorting-valid-notification-list-equal-values", async () => {
  fakeNotificationResponseItem2.id = 0;
  resultItem2.id = 0;
  HttpSender.get = jest
    .fn()
    .mockReturnValue(
      Promise.resolve([
        fakeNotificationResponseItem2,
        fakeNotificationResponseItem1,
      ]),
    );
  const result = await NotificationService.queryBackendForAllNotifications();
  expect(result).toStrictEqual([resultItem2, resultItem1]); // Order here is fine!
});

test("sorting-valid-notification-invalid", async () => {
  const result = NotificationService.sortList([resultItem1], "bad string");
  expect(result).toStrictEqual([]);
});

test("sorting-valid-notification-ascending", async () => {
  const result = NotificationService.sortList(
    [resultItem1, resultItem2],
    "asc",
  );
  expect(result).toStrictEqual([resultItem1, resultItem2]);
});

test("sorting-valid-notification-desc", async () => {
  const result = NotificationService.sortList(
    [resultItem1, resultItem2],
    "desc",
  );
  expect(result).toStrictEqual([resultItem2, resultItem1]);
});

test("check-all-read-false", async () => {
  fakeNotificationResponseItem1.read = true;

  HttpSender.get = jest
    .fn()
    .mockReturnValue(
      Promise.resolve([
        fakeNotificationResponseItem2,
        fakeNotificationResponseItem1,
      ]),
    );
  await NotificationService.queryBackendForAllNotifications();
  const result = NotificationService.areAllRead();
  expect(result).toStrictEqual(false);
});

test("check-all-read-true", async () => {
  fakeNotificationResponseItem1.read = true;
  fakeNotificationResponseItem2.read = true;

  HttpSender.get = jest
    .fn()
    .mockReturnValue(
      Promise.resolve([
        fakeNotificationResponseItem2,
        fakeNotificationResponseItem1,
      ]),
    );
  await NotificationService.queryBackendForAllNotifications();
  const result = NotificationService.areAllRead();
  expect(result).toStrictEqual(true);
});
