import React from "react";
import ShipNotification from "../../../../model/ShipNotification";
import ShipDetails from "../../../../model/ShipDetails";
import { CurrentPage } from "../../../../App";
import ShipsNotificationEntry from "./NotificationListEntry";
import List from "@mui/material/List";

import "../../../../styles/notifications/notificationList.css";

interface NotificationListProps {
  notifications: ShipNotification[];
  ships: ShipDetails[];
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
}

/**
 * Component that stores only the scrollable list of all notification entries.
 * It is used by both object details and notification list components.
 *
 * @param notifications a list of all notifications that are displayed
 * @param ships a list of all ships on the map
 * @param pageChanger page changer function
 * @param mapCenteringFun map centering function
 * @constructor
 */
function NotificationListWithoutTitle({
  notifications,
  ships,
  pageChanger,
  mapCenteringFun,
}: NotificationListProps) {
  const listEntries = notifications.map((notification, i) => {
    const shipDetails = ships
      .filter((x) => x.id === notifications[i].shipDetails.id)
      .slice()[0];
    return (
      <ShipsNotificationEntry
        key={i}
        notification={notification}
        shipDetails={shipDetails}
        pageChanger={pageChanger}
        mapCenteringFun={mapCenteringFun}
      />
    );
  });

  return <List id="notification-list-internal-container">{listEntries}</List>;
}

export default NotificationListWithoutTitle;
