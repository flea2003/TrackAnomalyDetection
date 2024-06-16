import ErrorNotificationService from "../../services/ErrorNotificationService";
import ShipDetails, { differentShipPositions } from "../../model/ShipDetails";

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

describe("different ship position function tests", () => {
  const createShip = (id: number, lat: number, lng: number) =>
    new ShipDetails(id, 0, lat, lng, "t", 0, "d", 0, "t", "p", 0, 0);

  // ship1 and ship2 have same position
  // ship1 and ship3 have same lat, but different lng
  // ship1 and ship4 have same lng, but different lat
  const ship1 = createShip(1, 0, 0);
  const ship2 = createShip(1, 0, 0);
  const ship3 = createShip(1, 0, 10);
  const ship4 = createShip(1, 10, 0);

  test("ships have same position", () => {
    expect(differentShipPositions(ship1, ship2)).toBe(false);
  });

  test("first ship is null", () => {
    expect(differentShipPositions(null, ship1)).toBe(false);
  });

  test("second ship is null", () => {
    expect(differentShipPositions(ship1, null)).toBe(false);
  });

  test("different longitude", () => {
    expect(differentShipPositions(ship1, ship3)).toBe(true);
  });

  test("latitude is different", () => {
    expect(differentShipPositions(ship1, ship4)).toBe(true);
  });
});
