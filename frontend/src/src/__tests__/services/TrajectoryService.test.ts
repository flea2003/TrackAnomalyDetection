import ErrorNotificationService from "../../services/ErrorNotificationService";
import HttpSender from "../../utils/communication/HttpSender";
import ShipDetails from "../../model/ShipDetails";
import TrajectoryResponseItem from "../../templates/TrajectoryResponseItem";
import TrajectoryService from "../../services/TrajectoryService";
import TrajectoryPoint from "../../model/TrajectoryPoint";

beforeEach(() => {
  // Make refreshState do nothing, so that it does not print to console.
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  ErrorNotificationService.refreshState = () => {};
});

const fakeAPIResponseItem1: TrajectoryResponseItem = {
  shipId: 1,
  longitude: 24,
  latitude: 48,
  timestamp: "time1",
  anomalyScore: 24,
};

const fakeAPIResponseItem2: TrajectoryResponseItem = {
  shipId: 1,
  longitude: 26,
  latitude: 50,
  timestamp: "time2",
  anomalyScore: 26,
};

const resultItem1 = new TrajectoryPoint(1, 24, 48, "time1", 24);
const resultItem2 = new TrajectoryPoint(1, 26, 50, "time2", 26);

test("backend-fetching-invalid", async () => {
  HttpSender.get = jest.fn().mockReturnValue(Promise.resolve([null]));
  const result =
    await TrajectoryService.queryBackendForSampledHistoryOfAShip(1);
  expect(result).toStrictEqual([]);
});

test("backend-fetching-empty-array", async () => {
  HttpSender.get = jest.fn().mockReturnValue(Promise.resolve([]));
  const result =
    await TrajectoryService.queryBackendForSampledHistoryOfAShip(1);
  expect(result).toStrictEqual([]);
});

test("backend-fetching-invalid-type", async () => {
  HttpSender.get = jest.fn().mockReturnValue(Promise.resolve("value"));
  const result =
    await TrajectoryService.queryBackendForSampledHistoryOfAShip(1);
  expect(result).toStrictEqual([]);
});

test("backend-fetching-valid-details", async () => {
  HttpSender.get = jest
    .fn()
    .mockReturnValue(
      Promise.resolve([fakeAPIResponseItem1, fakeAPIResponseItem2]),
    );
  const result =
    await TrajectoryService.queryBackendForSampledHistoryOfAShip(1);
  expect(result).toStrictEqual([resultItem1, resultItem2]);
});

test("should-query-false-undefined", async () => {
  expect(TrajectoryService.shouldQueryBackend(undefined)).toStrictEqual(false);
});

test("should-query-false-same-data", async () => {
  TrajectoryService.idOfCurrentlyStoredShip = 1;
  TrajectoryService.newestTimestampOfCurrentlyStoredShip = "time1";

  const ship = new ShipDetails(
    1,
    0,
    24,
    48,
    "time1",
    24,
    "d",
    0,
    "t",
    "p",
    0,
    0,
  );

  expect(TrajectoryService.shouldQueryBackend(ship)).toStrictEqual(false);
});

test("should-query-true-diff-id", async () => {
  TrajectoryService.idOfCurrentlyStoredShip = 1;
  TrajectoryService.newestTimestampOfCurrentlyStoredShip = "time1";

  const ship = new ShipDetails(
    2,
    0,
    24,
    48,
    "time1",
    24,
    "d",
    0,
    "t",
    "p",
    0,
    0,
  );

  expect(TrajectoryService.shouldQueryBackend(ship)).toStrictEqual(true);
});

test("should-query-true-diff-time", async () => {
  TrajectoryService.idOfCurrentlyStoredShip = 1;
  TrajectoryService.newestTimestampOfCurrentlyStoredShip = "time1";

  const ship = new ShipDetails(
    1,
    0,
    24,
    48,
    "time2",
    24,
    "d",
    0,
    "t",
    "p",
    0,
    0,
  );

  expect(TrajectoryService.shouldQueryBackend(ship)).toStrictEqual(true);
});
