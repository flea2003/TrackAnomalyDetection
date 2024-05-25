import React from "react";
import Stack from "@mui/material/Stack";
import { useState, useEffect } from "react";
import Map from "./components/Map/Map";
import ShipDetails from "./model/ShipDetails";
import ShipService from "./services/ShipService";
import { MapExportedMethodsType } from "./components/Map/Map";
import ErrorNotificationService from "./services/ErrorNotificationService";

import "./styles/common.css";
import Side from "./components/Side/Side";

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
  const [ships, setShips] = useState<ShipDetails[]>([]);
  const middleColumn = () => {
    switch (currentPage.currentPage) {
      case "anomalyList":
        return (
          <AnomalyList
            ships={Array.from(shipsWS.values())}
            pageChanger={pageChanger}
            mapCenteringFun={mapCenteringFun}
          />
        );
      case "objectDetails":
        return (
          <ObjectDetails
            ships={Array.from(shipsWS.values())}
            shipId={currentPage.shownShipId}
            pageChanger={pageChanger}
          />
        );
      case "notifications":
        return <div>Notifications</div>;
      case "settings":
        return <div>Settings</div>;
      case "none":
        return <div></div>;
    }
  };

  // // Put the ships as state
  // const [ships, setShips] = useState<ShipDetails[]>([]);
  //
  // // Every 1s update the anomaly score of all ships by querying the server
  // useEffect(() => {
  //   setInterval(() => {
  //     // Query for ships. When the results arrive, update the state
  //     ShipService.queryBackendForShipsArray().then(
  //       (shipsArray: ShipDetails[]) => {
  //         setShips(shipsArray);
  //       },
  //     );
  //   }, 1000);
  // }, []);

  const [shipsWS, setShipsWS] = useState<Map<number, ShipDetails>>(new Map());
  useEffect(() => {
    const socket = new WebSocket('ws://localhost:8081/ws');

    socket.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data) as ShipDetails;
        setShipsWS((prevShips) => {
          const updatedShipsMap: Map<number, ShipDetails> = new Map(prevShips);
          updatedShipsMap.set(message.id, message);
          return updatedShipsMap;
        });

      } catch (error) {
        console.log(error);
      }
    }

    socket.onopen = () => {
      console.log("WebSocket connection openned");
    }

    socket.onclose = () => {
      console.log("WebSocket connection closed");
    }

    socket.onerror = (error) => {
      console.log("Websocket connection error", error);
    }

    return () => {
      socket.close();
    }

  }, []);


  // Return the main view of the application
  return (
    <div className="App" id="root-div">
      <Stack direction="row">
        <Map ships={ships} pageChanger={pageChanger} ref={mapRef} />
        <Side
          currentPage={currentPage}
          ships={ships}
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
