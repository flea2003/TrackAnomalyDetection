import React from "react";
import Stack from "@mui/material/Stack";
import { Client } from "@stomp/stompjs";
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

  /**
   * Configure the WebSocket connection.
   */
  useEffect(() => {
    const stompClient = new Client({
      brokerURL: "ws://localhost:8081/details",
      debug: function (str) {
        console.log(str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    stompClient.onConnect = function (frame) {
      console.log("Connected: " + frame);
      stompClient.subscribe("/topic/details", function (message) {
        try {
          const apiResponse = JSON.parse(message.body) as APIResponseItem;
          const shipDetails =
            ShipService.extractCurrentShipDetails(apiResponse);
          console.log(shipDetails);
          setShips((prevShips) => {
            return new Map(prevShips).set(shipDetails.id, shipDetails);
          });
        } catch (error) {
          ErrorNotificationService.addError("Data fetching error");
        }
      });
    };

    stompClient.onWebSocketClose = function (closeEvent) {
      ErrorNotificationService.addError("Websocket connection error");
      setShips((prevMap) => {
        return new Map();
      });
    };

    // Leveraging the beforeConnect hook we fetch the latest state of the
    // backend table storing ship details before opening a WebSocket connection
    // with the backend STOMP broker
    stompClient.beforeConnect = async () => {
      ShipService.queryBackendForShipsArray().then(
        (shipsArray: ShipDetails[]) => {
          setShips(ShipService.constructMap(shipsArray));
        },
      );
    };

    stompClient.onStompError = function (frame) {
      ErrorNotificationService.addError(
        "Websocket broker error: " + frame.headers["message"],
      );
    };

    stompClient.activate();

    return () => {
      if (stompClient) {
        stompClient.deactivate();
      }
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
