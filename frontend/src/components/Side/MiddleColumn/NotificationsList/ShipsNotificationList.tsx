import { CurrentPage } from "../../../../App";
import Stack from "@mui/material/Stack";
import closeIcon from "../../../../assets/icons/close.svg";
import ErrorNotificationService, { ErrorNotification } from "../../../../services/ErrorNotificationService";
import List from "@mui/material/List";
import ErrorListEntry from "../ErrorNotifications/ErrorListEntry";
import React from "react";
import ShipNotification from "../../../../model/ShipNotification";
import ShipNotificationEntry from "./ShipsNotificationEntry";
import ShipDetails from "../../../../model/ShipDetails";
import AnomalyListEntry from "../AnomalyList/AnomalyListEntry";
import ShipsNotificationEntry from "./ShipsNotificationEntry";


import "../../../../styles/common.css";
import "../../../../styles/shipNotificationList.css";
import "../../../../styles/shipNotificationEntry.css";
import shipDetails from "../../../../model/ShipDetails";
import shipsNotificationService, { ShipsNotificationService } from "../../../../services/ShipsNotificationService";
import ShipsNotificationListWithoutTitle from "./ShipsNotificationListWithoutTitle";


interface NotificationListProps {
  notifications: ShipNotification[];
  ships: ShipDetails[];
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
}


function ShipsNotificationList({
                       notifications,
                       ships,
                       pageChanger,
                       mapCenteringFun,
                     }: NotificationListProps) {

  return (
    <Stack id="notification-list-container" data-testid="notification-list-container">
      <Stack id="notification-list-title-container" direction="row">
        <img
          src={closeIcon}
          alt="Close"
          id="notification-list-close-icon"
          data-testid="notification-list-close-icon"
          onClick={() => pageChanger({ currentPage: "none", shownShipId: -1 })}
        />
        <div id="notification-list-name-text">Notifications</div>
        <button
          id="notification-list-mark-all-button"
          onClick={() => {ShipsNotificationService.queryBackendToMarkAllNotificationsAsRead(notifications)}}>
          read
        </button>
      </Stack>
      <List
        id="notification-list-internal-container"
        style={{ maxHeight: "100%", overflow: "auto", padding: "0" }}
      >
        <ShipsNotificationListWithoutTitle
          notifications={notifications}
          ships={ships}
          pageChanger={pageChanger}
          mapCenteringFun={mapCenteringFun}
          />
      </List>
    </Stack>
  );
}

export default ShipsNotificationList;
