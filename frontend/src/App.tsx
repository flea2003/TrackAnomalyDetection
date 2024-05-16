import React from "react";
import Stack from "@mui/material/Stack";
import { useState, useEffect } from "react";
import Map from "./components/Map/Map";
import { MapExportedMethodsType } from "./components/Map/Map";
import AnomalyList from "./components/AnomalyList/AnomalyList";
import Sidebar from "./components/Sidebar/Sidebar";
import ObjectDetails from "./components/ObjectDetails/ObjectDetails";
import ShipDetails from "./model/ShipDetails";
import ShipService from "./services/ShipService";

import "./styles/common.css";

/**
 * Interface for storing the type of component that is currently displayed in the second column.
 */
export interface CurrentPage {
  currentPage: string;
  shownShipId: string;
}

function App() {
  // Create a reference to the map component
  const mapRef = React.useRef<MapExportedMethodsType>(null);

  // Create a function that passes a ship-centering function call to the map component
  const mapCenteringFun = (details: ShipDetails) => {
    if (mapRef.current !== null) {
      mapRef.current.centerMapOntoShip(details);
    }
  };

  // Create state for current page
  const [currentPage, setCurrentPage] = useState({
    currentPage: "none",
    shownShipId: "",
  } as CurrentPage);

  // Create function that is called when the current page needs to be changed
  const pageChanger = (newPage: CurrentPage) => {
    if (
      currentPage.currentPage !== "none" &&
      newPage.currentPage === currentPage.currentPage
    ) {
      // If we clicked the same icon for the second time
      setCurrentPage({ currentPage: "none", shownShipId: "" });
    } else {
      // Else, just set what was clicked
      setCurrentPage(newPage);
    }
  };

  const middleColumn = () => {
    switch (currentPage.currentPage) {
      case "anomalyList":
        return (
          <AnomalyList
            ships={ships}
            pageChanger={pageChanger}
            mapCenteringFun={mapCenteringFun}
          />
        );
      case "objectDetails":
        return (
          <ObjectDetails
            ships={ships}
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

  // Put the ships as state
  const [ships, setShips] = useState<ShipDetails[]>([]);

  // Every 1s update the anomaly score of all ships by querying the server
  useEffect(() => {
    setInterval(() => {
      // Query for ships. When the results arrive, update the state
      ShipService.queryBackendForShipsArray().then(
        (shipsArray: ShipDetails[]) => {
          setShips(shipsArray);
        },
      );
    }, 1000);
  }, []);

  // Return the main view of the application
  return (
    <div className="App" id="root-div">
      <Stack direction="row">
        <Map ships={ships} pageChanger={pageChanger} ref={mapRef} />
        {middleColumn()}
        <Sidebar pageChanger={pageChanger} />
      </Stack>
    </div>
  );
}

export default App;
