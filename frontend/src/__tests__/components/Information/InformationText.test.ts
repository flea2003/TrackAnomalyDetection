import { informationText } from "../../../components/Information/InformationText";

test("check all starts of the explanations", async () => {
  expect(informationText("anomalyList")).toContain("The Anomaly List displays");
  expect(informationText("objectDetails")).toContain(
    "The Object Details window",
  );
  expect(informationText("notificationList")).toContain(
    "The Notification List",
  );
  expect(informationText("notificationDetails")).toContain(
    "The Notification Details",
  );
  expect(informationText("errors")).toContain("The Error List");
  expect(informationText("settings")).toContain("In the future");
  expect(informationText("none")).toContain("The map displays");
  expect(informationText("sthElse")).toContain("");
});
