import TimeUtilities from "../../utils/TimeUtilities";
import ErrorNotificationService from "../../services/ErrorNotificationService";

beforeEach(() => {
  // Remove refreshState function, so it does nothing.
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  ErrorNotificationService.refreshState = () => {};
});

test("Check for invalid conversion", () => {
  const invalidTimestamp = "it's eleven o'clock";
  const result = TimeUtilities.computeTimeDifference(invalidTimestamp);
  expect(result).toBe("Not available");
});

test("Check for invalid time difference", () => {
  const mockCurrentTime = new Date("2004-03-27T01:01:00Z");
  const aisTimestamp = "2004-04-27T01:01:00Z";
  const spyOnErrorServiceMethod = jest.spyOn(
    ErrorNotificationService,
    "addWarning",
  );
  jest.spyOn(global, "Date").mockImplementation(() => mockCurrentTime);
  const result = TimeUtilities.computeTimeDifference(aisTimestamp);
  jest.restoreAllMocks();
  expect(spyOnErrorServiceMethod).toHaveBeenCalled();
  expect(result).toBe("Not available");
});

test("Check for valid time difference 1", () => {
  const mockCurrentTime = new Date("2004-03-27T02:01:00Z");
  const aisTimestamp = "2004-03-27T01:01:00Z";
  const spyOnErrorServiceMethod = jest.spyOn(
    ErrorNotificationService,
    "addWarning",
  );
  TimeUtilities.getCurrentTime = jest.fn().mockReturnValue(mockCurrentTime);
  const result = TimeUtilities.computeTimeDifference(aisTimestamp);
  expect(spyOnErrorServiceMethod).not.toHaveBeenCalled();
  expect(result).toBe("1h 0m");
  jest.resetAllMocks();
});

test("Check for valid time difference 2", () => {
  const mockCurrentTime = new Date("2004-03-28T02:01:00Z");
  const aisTimestamp = "2004-03-27T01:01:00Z";
  const spyOnErrorServiceMethod = jest.spyOn(
    ErrorNotificationService,
    "addWarning",
  );
  TimeUtilities.getCurrentTime = jest.fn().mockReturnValue(mockCurrentTime);
  const result = TimeUtilities.computeTimeDifference(aisTimestamp);
  expect(spyOnErrorServiceMethod).not.toHaveBeenCalled();
  expect(result).toBe("1d 1h 0m");
  jest.resetAllMocks();
});

test("Check for valid time difference 3", () => {
  const mockCurrentTime = new Date("2004-03-27T01:02:00Z");
  const aisTimestamp = "2004-03-27T01:01:00Z";
  const spyOnErrorServiceMethod = jest.spyOn(
    ErrorNotificationService,
    "addWarning",
  );
  TimeUtilities.getCurrentTime = jest.fn().mockReturnValue(mockCurrentTime);
  const result = TimeUtilities.computeTimeDifference(aisTimestamp);
  expect(spyOnErrorServiceMethod).not.toHaveBeenCalled();
  expect(result).toBe("1m");
  jest.resetAllMocks();
});

test("Prepend zero true", () => {
  expect(TimeUtilities.prependZero(1)).toStrictEqual("01");
});

test("Prepend zero false", () => {
  expect(TimeUtilities.prependZero(12)).toStrictEqual("12");
});

test("Reformat timestamp valid", () => {
  const aisTimestamp = "2004-04-27T01:01:00Z";
  expect(TimeUtilities.reformatTimestamp(aisTimestamp)).toStrictEqual(
    "2004-04-27 01:01",
  );
});

test("Reformat timestamp invalid 1", () => {
  const aisTimestamp = "-04-27T01:01:00Z";
  expect(TimeUtilities.reformatTimestamp(aisTimestamp)).toStrictEqual(
    "Not available",
  );
});

test("Get hours and minutes valid", () => {
  const aisTimestamp = "2004-03-27T01:02:00Z";
  expect(TimeUtilities.getHoursAndMinutes(aisTimestamp)).toStrictEqual("01:02");
});

test("Get hours and minutes invalid", () => {
  const aisTimestamp = "-2004-03-27T01:02:00Z";
  expect(TimeUtilities.getHoursAndMinutes(aisTimestamp)).toStrictEqual(
    "Not available",
  );
});

test("Compare Dates Equal", () => {
  const aisTimestamp1 = "2004-03-27T01:02:00Z";
  const aisTimestamp2 = "2004-03-27T01:02:00Z";
  expect(
    TimeUtilities.compareDates(aisTimestamp1, aisTimestamp2),
  ).toStrictEqual(-1);
});

test("Compare Dates Less", () => {
  const aisTimestamp1 = "2004-03-27T00:02:00Z";
  const aisTimestamp2 = "2004-03-27T01:02:00Z";
  expect(
    TimeUtilities.compareDates(aisTimestamp1, aisTimestamp2),
  ).toStrictEqual(1);
});
