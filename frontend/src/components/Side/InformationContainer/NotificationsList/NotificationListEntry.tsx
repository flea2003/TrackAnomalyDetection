import Stack from "@mui/material/Stack";
import React from "react";
import ShipDetails from "../../../../model/ShipDetails";
import { CurrentPage } from "../../../../App";
import ShipNotification from "../../../../model/ShipNotification";
import shipIcon from "../../../../assets/icons/anomaly-list/ship.png";
import bellIconNotRead from "../../../../assets/icons/regular-notifications/notification_bell_orange.png";
import bellIconRead from "../../../../assets/icons/regular-notifications/notification_bell.svg";
import { NotificationService } from "../../../../services/NotificationService";
import TimeUtilities from "../../../../utils/TimeUtilities";

import "../../../../styles/common.css";
import "../../../../styles/notifications/notificationList.css";
import "../../../../styles/notifications/notificationEntry.css";

interface NotificationEntryProps {
  notification: ShipNotification;
  shipDetails: ShipDetails;
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
}

/**
 * This component corresponds to a single entry in the Notifications List.
 * It displays the short description of the notification that it corresponds
 * to and also allows to get more detailed information by clicking on it. The
 * object to render is passed as a prop.
 *
 * @param notification notification whose details are displayed
 * @param shipDetails corresponding ship details for that notification
 * @param pageChanger page changer function
 * @param mapCenteringFun map centering function
 * @constructor
 */
function NotificationListEntry({
  notification,
  shipDetails,
  pageChanger,
  mapCenteringFun,
}: NotificationEntryProps) {
  const shipIconAltText = "Ship Icon";
  const readStatusClassName = notification.isRead
    ? "notification-list-entry-read"
    : "notification-list-entry-not-read";

  const id = notification.id;
  const shipAnomalyScore = notification.shipDetails.anomalyScore;
  const shipId = notification.shipDetails.id % 1000;
  const date = TimeUtilities.reformatTimestamp(
    notification.shipDetails.correspondingTimestamp,
  );

  // Once the 'read all' button is clicked on, all notifications should be set
  // as read in the backend
  const onClick = () => {
    pageChanger({ currentPage: "notificationDetails", shownItemId: id });
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
      <img
        src={getNotificationsBellType(notification.isRead).toString()}
        className="notification-list-bell-entry-icon"
        alt={shipIconAltText}
      />
      <div className="notification-list-ship-entry-icon-id-container">
        <span className="notification-list-ship-entry-icon-container">
          <img
            src={shipIcon}
            className="notification-list-ship-entry-icon"
            alt={shipIconAltText}
          />
        </span>
        <span className="notification-list-entry-id">#{shipId}</span>
      </div>
      {
        <span className="notification-list-entry-score">
          {shipAnomalyScore}%
        </span>
      }
      <span className="notification-list-entry-date">{date}</span>
    </Stack>
  );
}

/**
 * Changes the style of the notification bell background based on whether
 * there are any unread notifications
 */
function getNotificationsBellType(readStatus: boolean) {
  if (readStatus) {
    return bellIconRead;
  }
  return bellIconNotRead;
}

export default NotificationListEntry;
