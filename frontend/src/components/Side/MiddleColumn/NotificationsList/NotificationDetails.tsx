import React from "react";
import Stack from "@mui/material/Stack";
import List from "@mui/material/List";
import returnIcon from "../../../../assets/icons/back.svg";
import { CurrentPage } from "../../../../App";
import ErrorNotificationService from "../../../../services/ErrorNotificationService";
import ShipNotification from "../../../../model/ShipNotification";

import "../../../../styles/common.css";
import "../../../../styles/objectDetails.css";
import "../../../../styles/notificationDetails.css";
import ObjectDetailsEntry from "../ObjectDetails/ObjectDetailsEntry";

interface NotificationDetailsProps {
  allNotifications: ShipNotification[];
  notificationID: number;
  pageChanger: (currentPage: CurrentPage) => void;
}

/**
 * This component is in the second column of the main view of the application.
 * It displays the details of a selected notification in the middle column.
 * The object to whose details are to be displayed is passed as a prop.
 *
 * @param allNotifications all notifications extracted from the database
 * @param notificationID the ID of the notification whose details will be displayed
 * @param pageChanger page changer function
 * @constructor constructor
 */
function NotificationDetails({
  allNotifications,
  notificationID,
  pageChanger,
}: NotificationDetailsProps) {
  // Find the notification with the given ID.
  const notification = allNotifications.find((x) => x.id === notificationID);

  // If such notification is not (longer) present, show a message.
  if (notification === undefined) {
    ErrorNotificationService.addWarning(
      "Notification not found with ID: " + notificationID,
    );
    return notificationNotFoundElement();
  }

  return (
    <Stack id="object-details-container">
      <div className="object-details-title-container">
        {getReturnIcon(pageChanger)}
        <span className="object-details-title">
          Notification #{notification.id}
        </span>
      </div>
      <div className="notification-details-notifier">
        Ship details at the time of the anomaly:
      </div>
      <List className="notification-details-properties-list">
        {getPropertyElements(notification)}
      </List>
    </Stack>
  );
}

/**
 * Notifications error object that is used by error notifications functionality
 */
function notificationNotFoundElement() {
  return (
    <Stack id="object-details-container">
      <span className="object-details-title">
        Object ID:&nbsp;&nbsp;
        <span className="object-details-title-id">Not found</span>
      </span>
    </Stack>
  );
}

/**
 * Creates the UX component for notification details
 *
 * @param notification notification whose details are being displayed
 */
function getPropertyElements(notification: ShipNotification) {
  const properties = notification.getPropertyList();

  return properties.map((property) => {
    return (
      <ObjectDetailsEntry
        key={property.type}
        type={property.type}
        value={property.value.toString()}
      />
    );
  });
}

/**
 * Method that returns the return icon for the notification details

 * @param pageChanger page changer function
 */
function getReturnIcon(pageChanger: (currentPage: CurrentPage) => void) {
  const onReturnClicked = () => {
    pageChanger({ currentPage: "notificationList", shownItemId: -1 });
  };

  const returnIconAlt = "Return Icon";

  return (
    <img
      src={returnIcon}
      className="object-details-return-icon"
      onClick={onReturnClicked}
      alt={returnIconAlt}
    />
  );
}

export default NotificationDetails;
