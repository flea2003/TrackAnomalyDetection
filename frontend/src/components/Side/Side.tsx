import { CurrentPage } from "../../App";
import React, { forwardRef, useEffect, useImperativeHandle, useState } from "react";
import ShipDetails from "../../model/ShipDetails";
import Sidebar from "./Sidebar/Sidebar";
import MiddleColumn from "./MiddleColumn/MiddleColumn";
import ErrorNotificationService from "../../services/ErrorNotificationService";
import ShipNotification from "../../model/ShipNotification";
import { NotificationService } from "../../services/NotificationService";

import "../../styles/common.css";
import "../../styles/side.css";

import config from '../../configs/generalConfig.json';

interface SideProps {
  ships: ShipDetails[];
  mapCenteringFun: (details: ShipDetails) => void;
  setFilterThreshold: (value: number) => void;
  anomalyThreshold: number;
}

interface PageChangerRef {
  pageChanger: (currentPage: CurrentPage) => void;
}

/**
 * Function for side component
 *
 * @param ships list of all ships that are currently displayed
 * @param mapCenteringFun map centering function
 * @param setFilterThreshold function that sets the filtering threshold
 * @param anomalyThreshold the anomaly threshold that is used for filtering
 * @constructor
 */
const Side = forwardRef<PageChangerRef, SideProps>(
  ({
    ships,
    mapCenteringFun,
    setFilterThreshold,
    anomalyThreshold,
  }, ref) => {
    // Set up the ErrorNotificationService
    const [, setErrorNotificationState] = React.useState(
      ErrorNotificationService.getAllNotifications(),
    );
    ErrorNotificationService.initialize(setErrorNotificationState);

    // Set up the state for Notifications about ships
    const [notifications, setNotifications] = useState<ShipNotification[]>([]);

    // Update the notifications by querying the server frequently
    useEffect(() => {
      const intervalId = setInterval(() => {
        // Query for notifications. When the results arrive, update the state
        NotificationService.queryBackendForAllNotifications().then(
          (newNotifications: ShipNotification[]) => {
            if (!NotificationService.notificationArraysEqual(notifications, newNotifications)) {
              setNotifications(newNotifications);
            }
          },
        );
      }, config.notificationsRefreshMs);

      return () => {
        clearInterval(intervalId);
      }
    }, [notifications]);

    // Create state for current page
    const [currentPage, setCurrentPage] = useState({
      currentPage: "none",
      shownItemId: -1,
    } as CurrentPage);

    // Create function that is called when the current page needs to be changed
    const pageChanger = (newPage: CurrentPage) => {
      if (
        currentPage.currentPage !== "none" &&
        newPage.currentPage === currentPage.currentPage &&
        !areShipDetailsOpened(currentPage)
      ) {
        // If we clicked the same icon for the second time
        setCurrentPage({ currentPage: "none", shownItemId: -1 });
      } else {
        // Else, just set what was clicked
        setCurrentPage(newPage);
      }
    };

    useImperativeHandle(ref, () => ({pageChanger}));

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
)


function areShipDetailsOpened(currentPage: CurrentPage) {
  return (
    currentPage.currentPage === "objectDetails" &&
    currentPage.shownItemId !== -1
  );
}

Side.displayName = "Side";

export default Side;
export type {PageChangerRef};
