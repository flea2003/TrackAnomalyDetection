import React from "react";
import Stack from "@mui/material/Stack";
import { useState } from "react";
import LMap from "./components/Map/LMap";
import ShipDetails from "./model/ShipDetails";
import ShipService from "./services/ShipService";
import { MapExportedMethodsType } from "./components/Map/LMap";
import ErrorNotificationService from "./services/ErrorNotificationService";

import "./styles/common.css";
import Side from "./components/Side/Side";
import useWebSocketClient from "./utils/WebSocketClient";

/**
 * Interface for storing the type of component that is currently displayed in the second column.
 */
export interface CurrentPage {
  currentPage: string;
  shownShipId: number;
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
    shownShipId: -1,
  } as CurrentPage);

  // Create function that is called when the current page needs to be changed
  const pageChanger = (newPage: CurrentPage) => {
    if (
      currentPage.currentPage !== "none" &&
      newPage.currentPage === currentPage.currentPage &&
      !areShipDetailsOpened(currentPage)
    ) {
      // If we clicked the same icon for the second time
      setCurrentPage({ currentPage: "none", shownShipId: -1 });
    } else {
      // Else, just set what was clicked
      setCurrentPage(newPage);
    }
  };

  /**
   * Configure the state and the WebSocket connection with the backend server.
   */
  const ships = useWebSocketClient();

  // Return the main view of the application
  return (
    <div className="App" id="root-div">
      <Stack direction="row">
        <LMap
          ships={ShipService.sortList(Array.from(ships.values()), "desc")}
          pageChanger={pageChanger}
          ref={mapRef}
        />
        <Side
          currentPage={currentPage}
          ships={ShipService.sortList(Array.from(ships.values()), "desc")}
          pageChanger={pageChanger}
          mapCenteringFun={mapCenteringFun}
        />
      </Stack>
    </div>
  );
}

function areShipDetailsOpened(currentPage: CurrentPage) {
  return (
    currentPage.currentPage === "objectDetails" &&
    currentPage.shownShipId !== -1
  );
}

export default App;
