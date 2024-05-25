import React from "react";
import Stack from "@mui/material/Stack";
import { useState, useEffect } from "react";
import LMap from "./components/Map/LMap";
import ShipDetails from "./model/ShipDetails";
import ShipService from "./services/ShipService";
import { MapExportedMethodsType } from "./components/Map/LMap";
import ErrorNotificationService from "./services/ErrorNotificationService";

import "./styles/common.css";
import Side from "./components/Side/Side";
import APIResponseItem from "./templates/APIResponseItem";

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

  // Put the ships as state
  const [ships, setShips] = useState<Map<number, ShipDetails>>(new Map());

  // Leveraging the useEffect hook we fetch the latest state of the
  // backend table storing ship details whenever the main App component
  // is mounted or updated
  useEffect(() => {
    // Query for ship data in the backend
    ShipService.queryBackendForShipsArray().then(
      (shipsArray: ShipDetails[]) => {
        setShips(ShipService.constructMap(shipsArray));
      },
    );
  }, []);

  useEffect(() => {
    const socket = new WebSocket("ws://localhost:8081/ws");

    socket.onmessage = (event) => {
      try {
        const apiResponse = JSON.parse(event.data) as APIResponseItem;
        const shipDetails = ShipService.extractCurrentShipDetails(apiResponse);
        setShips((prevShips) => {
          const updatedShipsMap: Map<number, ShipDetails> = new Map(prevShips);
          updatedShipsMap.set(shipDetails.id, shipDetails);
          return updatedShipsMap;
        });
      } catch (error) {
        ErrorNotificationService.addError("WebSocket connection error");
      }
    };

    socket.onopen = () => {
      console.log("WebSocket connection opened");
    };

    socket.onclose = () => {
      ErrorNotificationService.addError("WebSocket connection closed");
    };

    socket.onerror = (error) => {
      ErrorNotificationService.addError("WebSocket connection error");
    };

    return () => {
      socket.close();
    };
  }, []);

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
