import { CurrentPage } from "../../App";
import React, {
  forwardRef,
  useEffect,
  useImperativeHandle,
  useState,
} from "react";
import ShipDetails from "../../model/ShipDetails";
import Sidebar from "./Sidebar/Sidebar";
import InformationContainer from "./InformationContainer/InformationContainer";
import ErrorNotificationService from "../../services/ErrorNotificationService";
import ShipNotification from "../../model/ShipNotification";
import { NotificationService } from "../../services/NotificationService";
import { Stack } from "@mui/material";

import "../../styles/common.css";
import "../../styles/side.css";

import config from "../../configs/generalConfig.json";
import InformationPopUp from "../Information/InformationPopUp";

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
  ({ ships, mapCenteringFun, setFilterThreshold, anomalyThreshold }, ref) => {
    // Set up the ErrorNotificationService
    const [, setErrorNotificationState] = React.useState(
      ErrorNotificationService.getAllNotifications(),
    );
    ErrorNotificationService.initialize(setErrorNotificationState);

    // Set up the state for Notifications about ships
    const [notifications, setNotifications] = useState<ShipNotification[]>([]);

    // Update the notifications by querying the server frequently
    useEffect(() => {
      const updateNotificationsFunc = () => {
        // Query for notifications. When the results arrive, update the state
        NotificationService.queryBackendForAllNotifications().then(
          (newNotifications: ShipNotification[]) => {
            if (newNotifications.length > notifications.length) {
              setNotifications(newNotifications);
            }
          },
        );
      };

      const intervalId = setInterval(
        updateNotificationsFunc,
        config.notificationsRefreshMs,
      );

      return () => {
        clearInterval(intervalId);
      };
    }, [notifications]);

    // Create state for current page
    const [currentPage, setCurrentPage] = useState(getPageChangerDefaultPage());
    const pageChanger = constructPageChanger(currentPage, setCurrentPage);

    // Save pageChanger in ref reachable by components above in the tree
    useImperativeHandle(ref, () => ({ pageChanger }));

    return (
      <div>
        <Stack direction="row" id="side-container">
          <InformationContainer
            currentPage={currentPage}
            ships={ships}
            notifications={notifications}
            pageChanger={pageChanger}
            mapCenteringFun={mapCenteringFun}
            setFilterThreshold={setFilterThreshold}
            anomalyThreshold={anomalyThreshold}
          />
          <Sidebar pageChanger={pageChanger} currentPage={currentPage} />
        </Stack>
        <InformationPopUp currentPage={currentPage} />
      </div>
    );
  },
);

function constructPageChanger(
  currentPage: CurrentPage,
  setCurrentPage: (
    value: ((prevState: CurrentPage) => CurrentPage) | CurrentPage,
  ) => void,
) {
  return (newPage: CurrentPage) => {
    if (
      currentPage.currentPage !== "none" &&
      newPage.currentPage === currentPage.currentPage &&
      !areShipDetailsOpened(currentPage)
    ) {
      // If we clicked the same icon for the second time
      setCurrentPage(getPageChangerDefaultPage());
    } else {
      // Else, just set what was clicked
      setCurrentPage(newPage);
    }
  };
}

function areShipDetailsOpened(currentPage: CurrentPage) {
  return (
    currentPage.currentPage === "objectDetails" &&
    currentPage.shownItemId !== -1
  );
}

function getPageChangerDefaultPage() {
  return {
    currentPage: "none",
    shownItemId: -1,
  } as CurrentPage;
}

Side.displayName = "Side";

export default Side;
export type { PageChangerRef };
