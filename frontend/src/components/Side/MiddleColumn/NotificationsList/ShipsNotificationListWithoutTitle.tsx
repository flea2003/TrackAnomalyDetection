import React from "react";
import ShipNotification from "../../../../model/ShipNotification";
import ShipDetails from "../../../../model/ShipDetails";
import { CurrentPage } from "../../../../App";
import ShipsNotificationEntry from "./ShipsNotificationEntry";

interface NotificationListProps {
  notifications: ShipNotification[];
  ships: ShipDetails[];
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
}

function ShipsNotificationListWithoutTitle({
                                             notifications,
                                             ships,
                                             pageChanger,
                                             mapCenteringFun,
                                           }: NotificationListProps) {
  const listEntries = notifications.map((notification, i) => {
    const shipDetails =  ships.filter(x => x.id === notifications[i].shipID).slice()[0];

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

  return <div>{listEntries}</div>;
}

export default ShipsNotificationListWithoutTitle;