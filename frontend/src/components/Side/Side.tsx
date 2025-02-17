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
import InformationPopUp from "../Information/InformationPopUp";

import "../../styles/common.css";
import "../../styles/side.css";

interface SideProps {
  ships: ShipDetails[];
  mapCenteringFun: (details: ShipDetails) => void;
  setFilterThreshold: (value: number) => void;
  anomalyThreshold: number;
  extractedFunctionsMap: React.RefObject<ExtractedFunctionsMap>;
  currentPage: CurrentPage;
  setCurrentPage: (value: CurrentPage) => void;
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
 * @param extractedFunctionsMap reference of functions passed from the LMap components
 * @param currentPage current page that is being dispalyed
 * @param setCurrentPage function for modifying the current page value
 * @constructor
 */
const Side = forwardRef<ExtractedFunctionsSide, SideProps>(
  (
    {
      ships,
      mapCenteringFun,
      setFilterThreshold,
      anomalyThreshold,
      extractedFunctionsMap,
      currentPage,
      setCurrentPage,
    },
    ref,
  ) => {
    // Set up the ErrorNotificationService
    const [, setErrorNotificationState] = React.useState(
      ErrorNotificationService.getAllNotifications(),
    );
    ErrorNotificationService.initialize(setErrorNotificationState);

    // Set up the state for notifications
    const [notifications, setNotifications] = useState<ShipNotification[]>([]);

    // Update the notifications by querying the server frequently
    useEffect(() => {
      const updateNotificationsFunc = () => {
        // Query for notifications from backend.
        // When the results arrive, update the state by setting notifications that
        // have been read to read
        NotificationService.queryBackendForAllNotifications().then(
          (newNotifications: ShipNotification[]) => {
            return setNotifications(
              NotificationService.updateNotifications(newNotifications),
            );
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
    const pageChanger = constructPageChanger(currentPage, setCurrentPage);

    // Save pageChanger in ref reachable by components above in the tree
    useImperativeHandle(ref, () => ({
      pageChanger,
      currentPage,
      notifications,
    }));

    return (
      <div>
        <Stack direction="row" id="side-container">
          <InformationContainer
            currentPage={currentPage}
            ships={ships}
            extractedFunctionsMap={extractedFunctionsMap}
            notifications={notifications}
            pageChanger={pageChanger}
            mapCenteringFun={mapCenteringFun}
            setFilterThreshold={setFilterThreshold}
            anomalyThreshold={anomalyThreshold}
          />
          <Sidebar
            pageChanger={pageChanger}
            currentPage={currentPage}
            notifications={notifications}
          />
        </Stack>
        <InformationPopUp currentPage={currentPage} />
      </div>
    );
  },
);

function constructPageChanger(
  currentPage: CurrentPage,
  setCurrentPage: (value: CurrentPage) => void,
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
export type { ExtractedFunctionsSide };
