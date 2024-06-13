import ShipUpdateBuffer from "../../services/ShipUpdateBuffer";
import ShipDetails from "../../model/ShipDetails";

beforeEach(() => {
  jest.resetAllMocks();
  ShipUpdateBuffer.resetBuffer();
});

afterEach(() => {
  ShipUpdateBuffer.resetBuffer();
});

const createShipWithId = (id: number) =>
  new ShipDetails(id, 0, 0, 0, "t", 0, "d", 0, "t", "p", 0, 0);

test("get and reset buffer test", () => {
  const ship1 = createShipWithId(1);
  const ship2 = createShipWithId(2);

  ShipUpdateBuffer.addToBuffer(ship1);
  ShipUpdateBuffer.addToBuffer(ship2);

  const result = ShipUpdateBuffer.getBufferedShipsAndReset();
  expect(Array.from(result)).toStrictEqual([
    [1, ship1],
    [2, ship2],
  ]);

  // after get and reset, there should be no buffered ships left
  const bufferedAgain = ShipUpdateBuffer.getBufferedShipsAndReset();
  expect(Array.from(bufferedAgain)).toStrictEqual([]);
});

test("reset buffer test", () => {
  const ship1 = createShipWithId(1);
  const ship2 = createShipWithId(2);

  ShipUpdateBuffer.addToBuffer(ship1);
  ShipUpdateBuffer.addToBuffer(ship2);
  ShipUpdateBuffer.resetBuffer();

  // after reset, there should be no buffered ships left
  const result = ShipUpdateBuffer.getBufferedShipsAndReset();
  expect(Array.from(result)).toStrictEqual([]);
});

test("buffer initially empty", () => {
  const result = ShipUpdateBuffer.getBufferedShipsAndReset();
  expect(Array.from(result)).toStrictEqual([]);
});
