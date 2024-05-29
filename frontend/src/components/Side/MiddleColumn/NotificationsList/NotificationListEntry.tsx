import Stack from "@mui/material/Stack";
import React from "react";
import ShipDetails from "../../../../model/ShipDetails";
import { CurrentPage } from "../../../../App";
import ShipNotification from "../../../../model/ShipNotification";
import shipIcon from "../../../../assets/icons/ship.png";
import "../../../../styles/common.css";
import "../../../../styles/notificationList.css";
import "../../../../styles/notificationEntry.css";
import { NotificationService } from "../../../../services/NotificationService";
import TimeUtilities from "../../../../utils/TimeUtilities";

interface NotificationEntryProps {
  notification: ShipNotification;
  shipDetails: ShipDetails;
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
}

/**
 * This component corresponds to a single entry in the Notifications List.
 * It displays the short description of the notification that it ccorresponds
 * to and also allows to get more detailed information by clicking on it. The
 * object to render is passed as a prop.
 */
function ShipNotificationEntry({
  notification,
  shipDetails,
  pageChanger,
  mapCenteringFun,
}: NotificationEntryProps) {
  const shipIconAltText = "Ship Icon";
  const readStatusClassName = notification.isRead
    ? "notification-list-entry-read"
    : "notification-list-entry-not-read";

  //console.log(readStatusClassName)

  const id = notification.id;
  const shipAnomalyScore = notification.shipDetails.anomalyScore;
  const shipId = notification.shipDetails.id % 1000;
  const date = TimeUtilities.reformatTimestamp(
    notification.shipDetails.correspondingTimestamp,
  );

  // Once the 'read all' button is clicked on, all notifications should be set
  // as read in the backend
  const onClick = () => {
    pageChanger({ currentPage: "notificationDetails", shownShipId: id });
    mapCenteringFun(shipDetails);
    NotificationService.queryBackendToMarkANotificationAsRead(notification);
  };

  return (
    <Stack
      direction="row"
      className={readStatusClassName}
      spacing={0}
      onClick={onClick}
    >
      <div className="notification-list-entry-icon-id-container">
        <span className="notification-list-entry-icon-container">
          <img
            src={shipIcon}
            className="notification-list-entry-icon"
            alt={shipIconAltText}
          />
        </span>
        <span className="notification-list-entry-id">#{shipId}</span>
      </div>
      <span className="notification-list-entry-score">{shipAnomalyScore}%</span>
      <span className="notification-list-entry-date">{date}</span>
    </Stack>
  );
}

export default ShipNotificationEntry;
