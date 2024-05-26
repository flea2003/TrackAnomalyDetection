import ErrorNotificationService, { ErrorNotification } from "../../../../services/ErrorNotificationService";
import Stack from "@mui/material/Stack";
import trashIcon from "../../../../assets/icons/trash.svg";
import React from "react";
import ShipDetails from "../../../../model/ShipDetails";
import { CurrentPage } from "../../../../App";
import ShipNotification from "../../../../model/ShipNotification";
import { calculateAnomalyColor } from "../../../../utils/AnomalyColorCalculator";
import shipIcon from "../../../../assets/icons/ship.png";

import "../../../../styles/common.css";
import "../../../../styles/shipNotificationList.css";
import "../../../../styles/shipNotificationEntry.css";


interface NotificationEntryProps {
  notification: ShipNotification;
  shipDetails: ShipDetails;
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
}

/**
 * This component is a single entry in the Error Notifications List.
 * It displays the (software) error that occurred.
 * The object to render is passed as a prop.
 *
 * @param notification ErrorNotification object which will be shown in this entry.
 */
function ShipNotificationEntry({ notification, shipDetails, pageChanger, mapCenteringFun }:
                                 NotificationEntryProps) {

  const shipIconAltText = "Ship Icon";

  const shipAnomalyScore = notification.anomalyScore;
  const message = notification.explanation;
  const shipId = notification.id % 1000;
  const date = notification.correspondingTimestamp;

  const onClick = () => {
    pageChanger({ currentPage: "notificationDetails", shownShipId: notification.id });
    mapCenteringFun(shipDetails);
  };

  return (
    <Stack
      direction="row"
      className="notification-list-entry"
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
      <span className="notification-list-entry-explanation">{message}</span>
    </Stack>
  );
}

export default ShipNotificationEntry;