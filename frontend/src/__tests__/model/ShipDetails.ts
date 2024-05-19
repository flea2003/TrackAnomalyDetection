import ShipDetails from "../../model/ShipDetails";

test("Rounding of latitude and longitude", () => {
  const fake = new ShipDetails(
    1,
    0,
    0.123456,
    0.12034,
    0,
    "test",
    "p1",
    1,
    350.0,
  );

  // Test that the rounding is done correctly. Assumes that the rounding is 1000
  expect(fake.getPropertyList()[2].value).toBe("0.123, 0.12");
});

test("getPropertyList returns an expected list", () => {
  const shipDetails = new ShipDetails(
    1,
    0,
    0.123456,
    0.12034,
    0,
    "test",
    "p1",
    1,
    350.0,
  );

  const properties = shipDetails.getPropertyList();

  // Test that the list has the correct size
  expect(properties.length).toBe(7);

  // check each property
  expect(properties[0]).toStrictEqual({ type: "Ship ID", value: "1" });
  expect(properties[1]).toStrictEqual({ type: "Explanation", value: "test" });
  expect(properties[2]).toStrictEqual({
    type: "Position",
    value: "0.123, 0.12",
  });
  expect(properties[3]).toStrictEqual({ type: "Speed", value: "350" });
  expect(properties[4]).toStrictEqual({ type: "Heading", value: "0" });
  expect(properties[5]).toStrictEqual({ type: "Departure", value: "p1" });
  expect(properties[6]).toStrictEqual({ type: "Course", value: "1" });
});
