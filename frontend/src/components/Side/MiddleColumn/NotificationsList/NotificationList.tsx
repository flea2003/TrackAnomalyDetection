import { CurrentPage } from "../../../../App";
import Stack from "@mui/material/Stack";
import closeIcon from "../../../../assets/icons/close.svg";
import React from "react";
import ShipNotification from "../../../../model/ShipNotification";
import ShipDetails from "../../../../model/ShipDetails";
import "../../../../styles/common.css";
import "../../../../styles/notificationList.css";
import "../../../../styles/notificationEntry.css";
import { NotificationService } from "../../../../services/NotificationService";
import NotificationListWithoutTitle from "./NotificationListWithoutTitle";

interface NotificationListProps {
  notifications: ShipNotification[];
  ships: ShipDetails[];
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
}

function NotificationList({
  notifications,
  ships,
  pageChanger,
  mapCenteringFun,
}: NotificationListProps) {
  // TODO: perhaps refactor all to separate functions!
  const image = (
    <img
      src={closeIcon}
      alt="Close"
      id="notification-list-close-icon"
      data-testid="notification-list-close-icon"
      onClick={() => pageChanger({ currentPage: "none", shownShipId: -1 })}
    />
  );

  return (
    <Stack
      id="notification-list-container"
      data-testid="notification-list-container"
    >
      <Stack id="notification-list-title-container" direction="row">
        {image}
        <div id="notification-list-name-text">Notifications</div>
        <div id="notification-list-button-button-div">
          <button
            id="notification-list-mark-all-button"
            onClick={() => {
              NotificationService.queryBackendToMarkAllNotificationsAsRead(
                notifications,
              );
            }}
          >
            Mark read
          </button>
        </div>
      </Stack>
      <NotificationListWithoutTitle
        notifications={notifications}
        ships={ships}
        pageChanger={pageChanger}
        mapCenteringFun={mapCenteringFun}
      />
    </Stack>
  );
}

export default NotificationList;
