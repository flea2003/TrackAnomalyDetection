import ErrorNotificationService from "../../services/ErrorNotificationService";
import ShipDetails from "../../model/ShipDetails";

beforeEach(() => {
  // Remove refreshState function, so it does nothing.
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  ErrorNotificationService.refreshState = () => {};
});

test("rounding latitude and longitude", () => {
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

  expect(fake.getRoundedLatitude()).toStrictEqual(0.123);
  expect(fake.getRoundedLongitude()).toStrictEqual(0.12);
});
