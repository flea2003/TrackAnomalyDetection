import HttpSender from "../../utils/communication/HttpSender";
import ShipService from "../../services/ShipService";
import APIResponseItem from "../../templates/APIResponseItem";
import ShipDetails from "../../model/ShipDetails";
import ErrorNotificationService from "../../services/ErrorNotificationService";
import spyOn = jest.spyOn;

beforeEach(() => {
  // Make refreshState do nothing, so that it does not print to console.
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  ErrorNotificationService.refreshState = () => {};
});

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

const fakeAPIResponseItem2: APIResponseItem = {
  currentAISSignal: {
    id: 2,
    speed: 350.0,
    longitude: 29.0,
    latitude: 47.0,
    course: 90,
    heading: 1,
    timestamp: "t2",
    departurePort: "p2",
  },
  currentAnomalyInformation: {
    id: 2,
    score: 2,
    explanation: "explanation",
    correspondingTimestamp: "t2",
  },
  maxAnomalyScoreInfo: {
    maxAnomalyScore: 2,
    correspondingTimestamp: "t2",
  },
};

const fakeAPIResponseItem3: APIResponseItem = {
  currentAISSignal: {
    id: 3,
    speed: 350.0,
    longitude: 29.0,
    latitude: 47.0,
    course: 90,
    heading: 1,
    timestamp: "t3",
    departurePort: "p3",
  },
  currentAnomalyInformation: {
    id: 3,
    score: 0.5,
    explanation: "explanation",
    correspondingTimestamp: "t3",
  },
  maxAnomalyScoreInfo: {
    maxAnomalyScore: 0.5,
    correspondingTimestamp: "t3",
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
  expect(result).toStrictEqual([]);
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
      "t1",
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
      "t1",
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
      "t1",
      -1,
      "Information not available (yet)",
      0,
      "Information not available (yet)",
      "p1",
      90,
      350.0,
    ),
  ]); // Now if anomaly score is not computed, we don't show it!
});

test("sorting-valid-list-descending-ascending", async () => {
  HttpSender.get = jest
    .fn()
    .mockReturnValue(
      Promise.resolve([
        fakeAPIResponseItem,
        fakeAPIResponseItem2,
        fakeAPIResponseItem3,
        fakeAPIResponseItem,
      ]),
    );
  const result = await ShipService.queryBackendForShipsArray();
  expect(result).toStrictEqual([
    new ShipDetails(
      2,
      1,
      47.0,
      29.0,
      "t2",
      2,
      "explanation",
      2,
      "t2",
      "p2",
      90,
      350.0,
    ),
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
    new ShipDetails(
      3,
      1,
      47.0,
      29.0,
      "t3",
      0.5,
      "explanation",
      0.5,
      "t3",
      "p3",
      90,
      350.0,
    ),
  ]);

  expect(ShipService.sortList(result, "asc")).toStrictEqual([
    new ShipDetails(
      3,
      1,
      47.0,
      29.0,
      "t3",
      0.5,
      "explanation",
      0.5,
      "t3",
      "p3",
      90,
      350.0,
    ),
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
    new ShipDetails(
      2,
      1,
      47.0,
      29.0,
      "t2",
      2,
      "explanation",
      2,
      "t2",
      "p2",
      90,
      350.0,
    ),
  ]);
});

test("sorting-invalid-order", () => {
  const spyOnErrorServiceMethod = spyOn(ErrorNotificationService, "addError");
  const result = ShipService.sortList([], "order");
  expect(spyOnErrorServiceMethod).toHaveBeenCalled();
  expect(result).toStrictEqual([]);
});

test("construct-map-empty-array", () => {
  const shipArray: ShipDetails[] = [];
  expect(ShipService.constructMap(shipArray)).toStrictEqual(
    new Map<number, ShipDetails>(),
  );
});

test("construct-map-rich-array", () => {
  const shipArray = [
    new ShipDetails(
      2,
      1,
      47.0,
      29.0,
      "t2",
      2,
      "explanation",
      2,
      "t2",
      "p2",
      90,
      350.0,
    ),
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
    new ShipDetails(
      3,
      1,
      47.0,
      29.0,
      "t3",
      0.5,
      "explanation",
      0.5,
      "t3",
      "p3",
      90,
      350.0,
    ),
  ];

  const constructedMap = ShipService.constructMap(shipArray);

  expect(Array.from(constructedMap.keys()).sort()).toStrictEqual([1, 2, 3]);
  expect(constructedMap.get(3)?.anomalyScore).toBe(0.5);
});

describe("sort list tests", () => {
  const createDummyShipWithId = (id: number, anomalyScore: number) => {
    return new ShipDetails(id, 0, 0, 0, "t", anomalyScore, "d", 0, "t", "p", 0, 0);
  }

  const ship1 = createDummyShipWithId(1, 5);
  const ship2 = createDummyShipWithId(2, 10);
  const ship3 = createDummyShipWithId(3, 6);

  test("desc sort implicitly", () => {
    expect(ShipService.sortList([ship1, ship2, ship3])).toStrictEqual([ship2, ship3, ship1]);
  })

  test("sort desc", () => {
    expect(ShipService.sortList([ship1, ship2, ship3], "desc")).toStrictEqual([ship2, ship3, ship1]);
  })
})
