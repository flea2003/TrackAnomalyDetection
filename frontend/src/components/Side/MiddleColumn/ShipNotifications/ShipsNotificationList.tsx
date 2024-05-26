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
  const listEntries = [];
  for (let i = 0; i < notifications.length; i++) {
    const shipDetails = ships.filter(x => x.id === notifications[i].shipID).slice()[0];
    listEntries.push(
      // eslint-disable-next-line react/jsx-no-undef
      <ShipsNotificationEntry
        key={i}
        notification={notifications[i]}
        shipDetails={shipDetails}
        pageChanger={pageChanger}
        mapCenteringFun={mapCenteringFun}
      />
    );
  }

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
        <div>Notifications</div>
      </Stack>
      <List
        id="notification-list-internal-container"
        style={{ maxHeight: "100%", overflow: "auto", padding: "0" }}
      >
        {listEntries}
      </List>
    </Stack>
  );
}

export default ShipsNotificationList;