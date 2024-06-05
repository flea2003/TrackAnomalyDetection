import React, { useEffect } from "react";
import Stack from "@mui/material/Stack";
import { useState } from "react";
import LMap from "./components/Map/LMap";
import ShipDetails from "./model/ShipDetails";
import { MapExportedMethodsType } from "./components/Map/LMap";
import ErrorNotificationService from "./services/ErrorNotificationService";
import "./styles/common.css";
import Side from "./components/Side/Side";
import useWebSocketClient from "./utils/communication/WebSocketClient";
import ShipNotification from "./model/ShipNotification";
import { NotificationService } from "./services/NotificationService";
import ShipService from "./services/ShipService";

/**
 * Interface for storing the type of component that is currently displayed in the second column.
 */
export interface CurrentPage {
  currentPage: string;
  shownItemId: number;
}

function App() {
  // Create a reference to the map component
  const mapRef = React.useRef<MapExportedMethodsType>(null);

  // Create a function that passes a ship-centering function call to the map component
  const mapCenteringFun = (details: ShipDetails) => {
    if (mapRef.current !== null) {
      mapRef.current.centerMapOntoShip(details);
    } else {
      ErrorNotificationService.addWarning("mapRef is null");
    }
  };

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

  /**
   * Configure the state and the WebSocket connection with the backend server.
   */
  const allShips = ShipService.sortList(
    Array.from(useWebSocketClient().values()),
    "desc",
  );

  // Put notifications as state
  const [notifications, setNotifications] = useState<ShipNotification[]>([]);

  // Put filter threshold as a state
  const [filterThreshold, setFilterThreshold] = useState<number>(0);

  // Create a separate array for displayed ships
  const displayedShips = allShips.filter(
    (x) => x.anomalyScore >= filterThreshold,
  );

  // Every 1s update the notifications by querying the server
  useEffect(() => {
    setInterval(() => {
      // Query for notifications. When the results arrive, update the state
      NotificationService.queryBackendForAllNotifications().then(
        (notificationsArray: ShipNotification[]) => {
          setNotifications(notificationsArray);
        },
      );
    }, 500);
  }, []);

  // Return the main view of the application
  return (
    <div className="App" id="root-div">
      <Stack direction="row">
        <LMap ships={displayedShips} pageChanger={pageChanger} ref={mapRef} />
        <Side
          currentPage={currentPage}
          ships={displayedShips}
          notifications={notifications}
          pageChanger={pageChanger}
          mapCenteringFun={mapCenteringFun}
          setFilterThreshold={setFilterThreshold}
          anomalyThreshold={filterThreshold}
        />
      </Stack>
    </div>
  );
}

function areShipDetailsOpened(currentPage: CurrentPage) {
  return (
    currentPage.currentPage === "objectDetails" &&
    currentPage.shownItemId !== -1
  );
}

export default App;
