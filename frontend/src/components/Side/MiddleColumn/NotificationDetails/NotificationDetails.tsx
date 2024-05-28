import React from "react";
import Stack from "@mui/material/Stack";
import List from "@mui/material/List";
import NotificationDetailsEntry from "./NotificationDetailsEntry";

import "../../../../styles/common.css";
import "../../../../styles/objectDetails.css";
import "../../../../styles/notificationDetails.css";
import returnIcon from "../../../../assets/icons/back.svg";
import { CurrentPage } from "../../../../App";
import ErrorNotificationService from "../../../../services/ErrorNotificationService";
import ShipNotification from "../../../../model/ShipNotification";

interface ObjectDetailsProps {
  notifications: ShipNotification[];
  notificationID: number;
  pageChanger: (currentPage: CurrentPage) => void;
}

/**
 * This component is in the second column of the main view of the application. It displays the details of a selected notification.
 * The object to whose details are to be displayed is passed as a prop.
 *
 * @param props properties passed to this component. Most importantly, it contains the notification information whose details to display.
 */
function NotificationDetails(props: ObjectDetailsProps) {
  // Extract the props
  const allNotifications = props.notifications;
  const notificationID = props.notificationID;
  const pageChanger = props.pageChanger;

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
          Notification ID: {notification.id}
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

function getPropertyElements(notification: ShipNotification) {
  const properties = notification.getPropertyList();

  return properties.map((property) => {
    return (
      <NotificationDetailsEntry
        key={property.type}
        type={property.type}
        value={property.value.toString()}
      />
    );
  });
}

/**
 * Method that returns the return icon for the notification details
 * TODO: perhaps make a separate class for it, as is is used for object details too
 * @param pageChanger
 */
function getReturnIcon(pageChanger: (currentPage: CurrentPage) => void) {
  const onReturnClicked = () => {
    pageChanger({ currentPage: "notificationList", shownShipId: -1 });
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
