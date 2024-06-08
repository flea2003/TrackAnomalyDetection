import { CurrentPage } from "../../../App";
import AnomalyList from "./AnomalyList/AnomalyList";
import ObjectDetails from "./ObjectDetails/ObjectDetails";
import React, { JSX } from "react";
import ShipDetails from "../../../model/ShipDetails";
import ErrorList from "./ErrorNotificationsList/ErrorList";
import ErrorNotificationService from "../../../services/ErrorNotificationService";
import NotificationList from "./NotificationsList/NotificationList";
import ShipNotification from "../../../model/ShipNotification";
import NotificationDetails from "./NotificationsList/NotificationDetails";
import "../../../styles/settings.css";


interface ObjectProps {
  currentPage: CurrentPage;
  ships: ShipDetails[];
  notifications: ShipNotification[];
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
  setFilterThreshold: (value: number) => void;
  anomalyThreshold: number;
}

/**
 * Function which returns the middle column component
 *
 * @param currentPage function for setting the current page
 * @param ships list of all ships that are currently displayed
 * @param notifications list of all notifications
 * @param pageChanger page changer functions
 * @param mapCenteringFun map centering function
 * @param setFilterThreshold function that sets the filtering threshold
 * @param anomalyThreshold the anomaly threshold that is used for filtering
 * @constructor
 */
function InformationContainer({
  currentPage,
  ships,
  notifications,
  pageChanger,
  mapCenteringFun,
  setFilterThreshold,
  anomalyThreshold,
}: ObjectProps): JSX.Element {
  switch (currentPage.currentPage) {
    case "anomalyList":
      return (
        <AnomalyList
          ships={ships}
          pageChanger={pageChanger}
          mapCenteringFun={mapCenteringFun}
          setFilterThreshold={setFilterThreshold}
          anomalyThreshold={anomalyThreshold}
        />
      );
    case "objectDetails":
      return (
        <ObjectDetails
          ships={ships}
          notifications={notifications}
          mapCenteringFun={mapCenteringFun}
          shipId={currentPage.shownItemId}
          pageChanger={pageChanger}
        />
      );
    case "notificationList":
      return (
        <NotificationList
          notifications={notifications}
          ships={ships}
          pageChanger={pageChanger}
          mapCenteringFun={mapCenteringFun}
        />
      );
    case "notificationDetails":
      return (
        <NotificationDetails
          allNotifications={notifications}
          notificationID={currentPage.shownItemId}
          pageChanger={pageChanger}
        />
      );
    case "settings":
      return <div className="settings-div">Settings</div>;
    case "errors":
      return <ErrorList pageChanger={pageChanger} />;
    case "none":
      return <div></div>;
    default: {
      ErrorNotificationService.addWarning(
        "InformationContainer required page not found: " + currentPage.currentPage,
      );
      return <div></div>;
    }
  }
}

export default InformationContainer;
