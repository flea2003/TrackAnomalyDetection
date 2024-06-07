import { CurrentPage } from "../../../../App";
import Stack from "@mui/material/Stack";
import closeIcon from "../../../../assets/icons/close.svg";
import React from "react";
import ShipNotification from "../../../../model/ShipNotification";
import ShipDetails from "../../../../model/ShipDetails";
import { NotificationService } from "../../../../services/NotificationService";
import NotificationListWithoutTitle from "./NotificationListWithoutTitle";

import "../../../../styles/common.css";
import "../../../../styles/notificationList.css";
import "../../../../styles/notificationEntry.css";

interface NotificationListProps {
  notifications: ShipNotification[];
  ships: ShipDetails[];
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
}

/**
 * Visual component for scrollable list of notifications that were created, together
 * with all other information (title, mark-all-as-read button, likely threshold choice), all
 * stored in the middle column of the design.
 *
 * @param notifications a list of all notifications extracted from the database
 * @param ships a list of all ships and their details. Needed for other functions.
 * @param pageChanger page changer
 * @param mapCenteringFun map centering function
 * @constructor
 */
function NotificationList({
  notifications,
  ships,
  pageChanger,
  mapCenteringFun,
}: NotificationListProps) {
  const image = (
    <img
      src={closeIcon}
      alt="Close"
      id="notification-list-close-icon"
      data-testid="notification-list-close-icon"
      onClick={() => pageChanger({ currentPage: "none", shownItemId: -1 })}
    />
  );

  return (
    <Stack
      id="notification-list-container"
      data-testid="notification-list-container"
      direction="column"
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
            Mark all as read
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
