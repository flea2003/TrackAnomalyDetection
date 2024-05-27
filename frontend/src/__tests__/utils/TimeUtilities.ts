import TimeUtilities from "../../utils/TimeUtilities";
import ErrorNotificationService from "../../services/ErrorNotificationService";

test("Check for invalid conversion", () => {
  const invalidTimestamp = "it's eleven o'clock";
  const spyOnErrorServiceMethod = jest.spyOn(
    ErrorNotificationService,
    "addError",
  );
  const result = TimeUtilities.computeTimeDifference(invalidTimestamp);
  expect(spyOnErrorServiceMethod).toHaveBeenCalled();
  expect(result).toBe("Not available");
});

test("Check for invalid time difference", () => {
  const mockCurrentTime = new Date("2004-03-27T01:01:00Z");
  const aisTimestamp = "2004-04-27T01:01:00Z";
  const spyOnErrorServiceMethod = jest.spyOn(
    ErrorNotificationService,
    "addError",
  );
  jest.spyOn(global, "Date").mockImplementation(() => mockCurrentTime);
  const result = TimeUtilities.computeTimeDifference(aisTimestamp);
  jest.restoreAllMocks();
  expect(spyOnErrorServiceMethod).toHaveBeenCalled();
  expect(result).toBe("Not available");
});

test("Check for valid time difference", () => {
  const mockCurrentTime = new Date("2004-03-27T02:01:00Z");
  const aisTimestamp = "2004-03-27T01:01:00Z";
  const spyOnErrorServiceMethod = jest.spyOn(
    ErrorNotificationService,
    "addError",
  );
  TimeUtilities.getCurrentTime = jest.fn().mockReturnValue(mockCurrentTime);
  const result = TimeUtilities.computeTimeDifference(aisTimestamp);
  expect(spyOnErrorServiceMethod).not.toHaveBeenCalled();
  expect(result).toBe("0d, 1h, 0m");
  jest.resetAllMocks();
});
