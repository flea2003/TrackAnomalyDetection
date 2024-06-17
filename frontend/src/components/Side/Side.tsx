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
import config from "../../configs/generalConfig.json";
import { ExtractedFunctionsMap } from "../Map/LMap";

import "../../styles/common.css";
import "../../styles/side.css";
import TrajectoryPoint from "../../model/TrajectoryPoint";

interface SideProps {
  ships: ShipDetails[];
  displayedTrajectoryAndNotifications: TrajectoryPoint[][];
  mapCenteringFun: (details: ShipDetails) => void;
  setFilterThreshold: (value: number) => void;
  anomalyThreshold: number;
  extractedFunctionsMap: React.RefObject<ExtractedFunctionsMap>;
}

interface ExtractedFunctionsSide {
  pageChanger: (currentPage: CurrentPage) => void;
  notifications: ShipNotification[];
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
const Side = forwardRef<ExtractedFunctionsSide, SideProps>(
  (
    {
      ships,
      displayedTrajectoryAndNotifications,
      mapCenteringFun,
      setFilterThreshold,
      anomalyThreshold,
      extractedFunctionsMap,
    },
    ref,
  ) => {
    // Set up the ErrorNotificationService
    const [, setErrorNotificationState] = React.useState(
      ErrorNotificationService.getAllNotifications(),
    );
    ErrorNotificationService.initialize(setErrorNotificationState);

    // Set up the state for Notifications about ships
    const [notifications, setNotifications] = useState<ShipNotification[]>([]);

    // Create state for current page
    const [currentPage, setCurrentPage] = useState(getPageChangerDefaultPage());

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

    // Construct page changer function
    const pageChanger = constructPageChanger(
      currentPage,
      setCurrentPage,
      extractedFunctionsMap.current?.setCurrentPageMap,
    );

    // Save pageChanger in ref reachable by components above in the tree
    useImperativeHandle(ref, () => ({
      pageChanger,
      currentPage,
      notifications,
    }));

    return (
      <Stack direction="row" id="side-container">
        <InformationContainer
          currentPage={currentPage}
          ships={ships}
          displayedTrajectoryAndNotifications={
            displayedTrajectoryAndNotifications
          }
          notifications={notifications}
          pageChanger={pageChanger}
          mapCenteringFun={mapCenteringFun}
          setFilterThreshold={setFilterThreshold}
          anomalyThreshold={anomalyThreshold}
        />
        <Sidebar pageChanger={pageChanger} currentPage={currentPage} />
      </Stack>
    );
  },
);

function constructPageChanger(
  currentPage: CurrentPage,
  setCurrentPage: (
    value: ((prevState: CurrentPage) => CurrentPage) | CurrentPage,
  ) => void,
  setCurrentPageMap: ((page: CurrentPage) => void) | undefined,
) {
  return (newPage: CurrentPage) => {
    if (
      currentPage.currentPage !== "none" &&
      newPage.currentPage === currentPage.currentPage &&
      !areShipDetailsOpened(currentPage)
    ) {
      // If we clicked the same icon for the second time
      setCurrentPage(getPageChangerDefaultPage());

      // Set the needed page to the
      if (setCurrentPageMap !== undefined)
        setCurrentPageMap(getPageChangerDefaultPage());
    } else {
      // Else, just set what was clicked
      setCurrentPage(newPage);

      // Set the needed page to the
      if (setCurrentPageMap !== undefined) setCurrentPageMap(newPage);
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
export type { ExtractedFunctionsSide };
