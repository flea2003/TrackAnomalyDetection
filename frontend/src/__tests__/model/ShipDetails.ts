import ShipDetails from "../../model/ShipDetails";
import ErrorNotificationService from "../../services/ErrorNotificationService";

beforeEach(() => {
  // Remove refreshState function, so it does nothing.
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  ErrorNotificationService.refreshState = () => {};
});

test("Test ship details", () => {
  const fake = new ShipDetails(
    1,
    0,
    0.123456,
    0.12034,
    "t1",
    0,
    "test",
    0,
    "time",
    "p1",
    1,
    350.0,
  );

  // Test that the rounding is done correctly. Assumes that the rounding is 1000
  expect(fake.getPropertyList()).toStrictEqual([
    {
      type: "Anomaly Score",
      value: "0%",
    },
    {
      type: "Explanation",
      value: "test",
    },
    {
      type: "Last AIS signal",
      value: "Not available ago",
    },
    {
      type: "Highest Recorded Anomaly Score",
      value: "0%",
    },
    {
      type: "Timestamp of the Highest Anomaly Score",
      value: "time",
    },
    {
      type: "Heading",
      value: "0",
    },
    {
      type: "Departure Port",
      value: "p1",
    },
    {
      type: "Course",
      value: "1",
    },
    {
      type: "Position",
      value: "0.123, 0.12",
    },
    {
      type: "Speed",
      value: "350",
    },
  ]);
});
