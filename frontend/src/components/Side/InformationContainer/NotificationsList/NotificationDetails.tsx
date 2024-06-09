import React, { useState } from "react";
import Stack from "@mui/material/Stack";
import returnIcon from "../../../../assets/icons/helper-icons/back.svg";
import { CurrentPage } from "../../../../App";
import ErrorNotificationService from "../../../../services/ErrorNotificationService";
import ShipNotification from "../../../../model/ShipNotification";
import AnomalyDetails from "../ObjectDetails/AnomalyDetails";
import AISDetails from "../ObjectDetails/AISDetails";

import "../../../../styles/common.css";
import "../../../../styles/object-details/objectDetails.css";
import "../../../../styles/notifications/notificationDetails.css";

interface NotificationDetailsProps {
  allNotifications: ShipNotification[];
  notificationID: number;
  pageChanger: (currentPage: CurrentPage) => void;
}

/**
 * This component is in the second column of the main view of the application.
 * It displays the details of a selected notification in the middle column..
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
  const [displayedAnomalyInfo, setDisplayedAnomalyInfo] = useState(true);
  const [displayedAIS, setDisplayedAIS] = useState(false);

  const changeAnomalyInfo = () => {
    setDisplayedAnomalyInfo((x) => true);
    setDisplayedAIS((x) => false);
  };

  const changeAIS = () => {
    setDisplayedAnomalyInfo((x) => false);
    setDisplayedAIS((x) => true);
  };

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
    <Stack className="notification-details-container">
      <div className="notification-details-title-container">
        {getReturnIcon(pageChanger)}
        <div className="notification-title-div">
          Notification #{notification.id}
        </div>
      </div>
      <div className="ship-id-div">Ship #{notification.shipDetails.id}</div>
      <Stack direction="column" className="menu-info-container">
        <Stack direction="row" className="menu-container">
          <div
            onClick={changeAnomalyInfo}
            className={
              displayedAnomalyInfo
                ? "notification-displayed"
                : "notification-not-displayed"
            }
          >
            Information
          </div>
          <div
            onClick={changeAIS}
            className={
              displayedAIS
                ? "notification-displayed"
                : "notification-not-displayed"
            }
          >
            AIS
          </div>
        </Stack>
        <Stack className="info-container">
          {displayedAnomalyInfo && (
            <AnomalyDetails
              ship={notification.shipDetails}
              addAnomalyScore={true}
            />
          )}
          {displayedAIS && <AISDetails ship={notification.shipDetails} />}
        </Stack>
      </Stack>
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
