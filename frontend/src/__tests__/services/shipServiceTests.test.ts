import HttpSender from "../../utils/HttpSender";
import ShipService from "../../services/ShipService";
import APIResponseItem from "../../templates/APIResponseItem";
import ShipDetails from "../../model/ShipDetails";

const fakeAPIResponseItem: APIResponseItem = {
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
};

const fakeAPIResItemNoAIS = {
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
};

const fakeAPIResItemNoAnomalyInfo = {
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
};

test("backend-fetching-invalid", async () => {
  HttpSender.get = jest.fn().mockReturnValue(Promise.resolve([null]));
  const result = await ShipService.queryBackendForShipsArray();
  expect(result).toStrictEqual([ShipService.dummyShipDetails()]);
});

test("backend-fetching-empty-array", async () => {
  HttpSender.get = jest.fn().mockReturnValue(Promise.resolve([]));
  const result = await ShipService.queryBackendForShipsArray();
  expect(result).toStrictEqual([]);
});

test("backend-fetching-invalid-type", async () => {
  HttpSender.get = jest.fn().mockReturnValue(Promise.resolve("value"));
  const result = await ShipService.queryBackendForShipsArray();
  expect(result).toStrictEqual([]);
});

test("backend-fetching-valid-details", async () => {
  HttpSender.get = jest
    .fn()
    .mockReturnValue(Promise.resolve([fakeAPIResponseItem]));
  const result = await ShipService.queryBackendForShipsArray();
  expect(result).toStrictEqual([
    new ShipDetails(
      1,
      1,
      47.0,
      29.0,
      1,
      "explanation",
      1,
      "t1",
      "p1",
      90,
      350.0,
    ),
  ]);
});

test("backend-fetching-null-ais", async () => {
  HttpSender.get = jest
    .fn()
    .mockReturnValue(Promise.resolve([fakeAPIResItemNoAIS]));
  const result = await ShipService.queryBackendForShipsArray();
  expect(result).toStrictEqual([
    new ShipDetails(
      1,
      0,
      0,
      0,
      1,
      "explanation",
      1,
      "t1",
      "Information not available (yet)",
      0,
      0,
    ),
  ]);
});

test("backend-fetching-null-anomaly-info", async () => {
  HttpSender.get = jest
    .fn()
    .mockReturnValue(Promise.resolve([fakeAPIResItemNoAnomalyInfo]));
  const result = await ShipService.queryBackendForShipsArray();
  expect(result).toStrictEqual([
    new ShipDetails(
      1,
      1,
      47.0,
      29.0,
      -1,
      "Information not available (yet)",
      0,
      "Information not available (yet)",
      "p1",
      90,
      350.0,
    ),
  ]);
});
