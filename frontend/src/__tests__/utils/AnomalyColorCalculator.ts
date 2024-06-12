import { calculateAnomalyColor } from "../../utils/AnomalyColorCalculator";

test("Complete green (if not anomalous at all)", () => {
  expect(calculateAnomalyColor(0, false)).toBe("rgba(0, 255, 0, 0.4)");
});

test("Complete red (if very anomalous)", () => {
  expect(calculateAnomalyColor(100, false)).toBe("rgba(255, 0, 0, 0.4)");
});

test("Anomaly middle (50)", () => {
  expect(calculateAnomalyColor(50, false)).toBe("rgba(127, 127, 0, 0.4)");
});

test("Brighter colours test", () => {
  expect(calculateAnomalyColor(0, true)).toBe("rgba(0, 255, 0, 1)");
})
