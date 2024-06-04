import { CurrentPage } from "../../App";
import React, { JSX } from "react";
import ShipDetails from "../../model/ShipDetails";
import Sidebar from "./Sidebar/Sidebar";
import MiddleColumn from "./MiddleColumn/MiddleColumn";
import ErrorNotificationService from "../../services/ErrorNotificationService";
import ShipNotification from "../../model/ShipNotification";

import "../../styles/common.css";
import "../../styles/side.css";

interface SideProps {
  currentPage: CurrentPage;
  ships: ShipDetails[];
  notifications: ShipNotification[];
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
  setFilterThreshold: (value: number) => void;
  anomalyThreshold: number;
}

/**
 * Function for side component
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
function Side({
  currentPage,
  ships,
  notifications,
  pageChanger,
  mapCenteringFun,
  setFilterThreshold,
  anomalyThreshold,
}: SideProps): JSX.Element {
  // Set up the ErrorNotificationService
  const [, setErrorNotificationState] = React.useState(
    ErrorNotificationService.getAllNotifications(),
  );
  ErrorNotificationService.initialize(setErrorNotificationState);

  return (
    <>
      <MiddleColumn
        currentPage={currentPage}
        ships={ships}
        notifications={notifications}
        pageChanger={pageChanger}
        mapCenteringFun={mapCenteringFun}
        setFilterThreshold={setFilterThreshold}
        anomalyThreshold={anomalyThreshold}
      />
      <Sidebar pageChanger={pageChanger} />
    </>
  );
}

export default Side;
