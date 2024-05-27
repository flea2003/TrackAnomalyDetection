import React, { useEffect, useState } from "react";
import Stack from "@mui/material/Stack";
import List from "@mui/material/List";
import ShipDetails from "../../../../model/ShipDetails";
import NotificationDetailsEntry from "./NotificationDetailsEntry";

import "../../../../styles/common.css";
import "../../../../styles/objectDetails.css";
import returnIcon from "../../../../assets/icons/back.svg";
import { CurrentPage } from "../../../../App";
import ErrorNotificationService from "../../../../services/ErrorNotificationService";
import ShipNotification from "../../../../model/ShipNotification";
import shipNotification from "../../../../model/ShipNotification";
import { ListGroup } from "react-bootstrap";
import { ShipsNotificationService } from "../../../../services/ShipsNotificationService";
import ShipsNotificationList from "../NotificationsList/ShipsNotificationList";
import ShipsNotificationListWithoutTitle from "../NotificationsList/ShipsNotificationListWithoutTitle";

interface ObjectDetailsProps {
  notifications: ShipNotification[],
  notificationID: number,
  pageChanger: (currentPage: CurrentPage) => void,
  mapCenteringFun: (details: ShipDetails) => void,
  ships: ShipDetails[],
}

/**
 * This component is the second column of the main view of the application. It displays the details of a selected object.
 * The object to whose details are to be displayed is passed as a prop.
 *
 * @param props properties passed to this component. Most importantly, it contains the ship object whose details to display.
 */
function NotificationDetails(props: ObjectDetailsProps) {
  // Extract the props
  const allNotifications = props.notifications;
  const notificationID = props.notificationID;
  const pageChanger = props.pageChanger;

  // Find the ship with the given ID in the map. If such ship is not (longer) present, show a message.
  const notification = allNotifications.find((x) => x.id === notificationID);
  const shipNotifications = allNotifications.filter((x) => x.shipID === notification?.shipID);


  if (notification === undefined) {
    ErrorNotificationService.addWarning("Notification not found with ID: " + notificationID);
    return notificationNotFoundElement();
  }

  return (
    <Stack id="object-details-container">
      <div className="object-details-title-container">
        {getReturnIcon(pageChanger)}
        <span className="object-details-title">Score {notification.anomalyScore}%</span>
      </div>
      <List
        style={{ maxHeight: "100%", overflow: "auto" }}
        className="object-details-list"
      >
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
