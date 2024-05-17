import ShipDetails from "../../model/ShipDetails";

test("Rounding of latitude and longitude", () => {
  const fake = new ShipDetails(
    "1",
    0,
    0.123456,
    0.12034,
    0,
    "test",
    -1,
    "p1",
    1,
    350.0,
  );

  // Test that the rounding is done correctly. Assumes that the rounding is 1000
  expect(fake.getPropertyList()[7].value).toBe("0.123");
  expect(fake.getPropertyList()[8].value).toBe("0.12");
});

test("getPropertyList returns a list of correct size", () => {
  const fake = new ShipDetails(
    "1",
    0,
    0.123456,
    0.12034,
    0,
    "test",
    -1,
    "p1",
    1,
    350.0,
  );

  // Test that the list has the correct size
  expect(fake.getPropertyList().length).toBe(10);
});

test("getPropertyList returns correct id", () => {
  const fake = new ShipDetails(
    "1",
    0,
    0.123456,
    0.12034,
    0,
    "test",
    -1,
    "p1",
    1,
    350.0,
  );

  // Test that the id is correct
  expect(fake.getPropertyList()[0].value).toBe("Ship");
});

test("getPropertyList returns correct anomaly score", () => {
  const fake = new ShipDetails(
    "1",
    0,
    0.123456,
    0.12034,
    0,
    "test",
    -1,
    "p1",
    1,
    350.0,
  );

  // Test that the anomaly score is correct
  expect(fake.getPropertyList()[1].value).toBe("0%");
});

test("getPropertyList returns correct explanation", () => {
  const fake = new ShipDetails(
    "1",
    0,
    0.123456,
    0.12034,
    0,
    "test",
    -1,
    "p1",
    1,
    350.0,
  );

  // Test that the explanation is correct
  expect(fake.getPropertyList()[2].value).toBe("test");
});

test("getPropertyList returns correct heading", () => {
  const fake = new ShipDetails(
    "1",
    0,
    0.123456,
    0.12034,
    0,
    "test",
    -1,
    "p1",
    1,
    350.0,
  );

  // Test that the heading is correct
  expect(fake.getPropertyList()[9].value).toBe("0");
});
