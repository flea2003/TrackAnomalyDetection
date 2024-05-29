import ShipDetails from "../../model/ShipDetails";
import ErrorNotificationService from "../../services/ErrorNotificationService";
import ShipNotification from "../../model/ShipNotification";
import notificationDetails from "../../components/Side/MiddleColumn/NotificationDetails/NotificationDetails";

beforeEach(() => {
  // Remove refreshState function, so it does nothing.
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  ErrorNotificationService.refreshState = () => {};
});

const shipNotification = new ShipNotification(
  0,
  false,
  new ShipDetails(
    1,
    1,
    47.0,
    29.0,
    "t1",
    1,
    "explanation",
    1,
    "t1",
    "p1",
    90,
    350.0,
  ),
);

test("Rounding of latitude and longitude", () => {
  expect(shipNotification.getPropertyList()).toStrictEqual([
    {
      type: "Ship ID",
      value: 1,
    },
    {
      type: "Time of the anomaly",
      value: "t1",
    },
    {
      type: "Anomaly Score",
      value: "1%",
    },
    {
      type: "Explanation",
      value: "explanation",
    },
    {
      type: "Highest Recorded Anomaly Score At The Time",
      value: "1%",
    },
    {
      type: "Heading",
      value: "1",
    },
    {
      type: "Departure Port",
      value: "p1",
    },
    {
      type: "Course",
      value: "90",
    },
    {
      type: "Position",
      value: "47, 29",
    },
    {
      type: "Speed",
      value: "350",
    },
  ]);
});
