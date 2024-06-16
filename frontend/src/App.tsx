import React, { useEffect } from "react";
import { useState } from "react";
import LMap from "./components/Map/LMap";
import ShipDetails from "./model/ShipDetails";
import generalConfig from "./configs/generalConfig.json";
import { ExtractedFunctionsMap } from "./components/Map/LMap";
import ErrorNotificationService from "./services/ErrorNotificationService";
import "./styles/common.css";
import Side, { ExtractedFunctionsSide } from "./components/Side/Side";
import ShipService from "./services/ShipService";
import "./styles/common.css";

/**
 * Interface for storing the type of component that is currently displayed in the second column.
 */
export interface CurrentPage {
  currentPage: string;
  shownItemId: number;
}

function App() {
  // References to the `map` and `pageChanger` objects. Will be assigned later.
  const extractedFunctionsMap = React.useRef<ExtractedFunctionsMap>(null);
  const extractedFunctionsSide = React.useRef<ExtractedFunctionsSide>(null);

  // Create a function that passes a ship-centering function call to the map component
  const mapCenteringFun = (details: ShipDetails) => {
    if (extractedFunctionsMap.current !== null) {
      extractedFunctionsMap.current.centerMapOntoShip(details);
    } else {
      ErrorNotificationService.addWarning("extractedFunctionsMap is null");
    }
  };

  const [rawShips, setRawShips] = useState<ShipDetails[]>([]);

  // Use effect to query for the ships every 1000ms
  useEffect(() => {
    const intervalId = setInterval(() => {
      ShipService.queryBackendForShipsArray().then(
        (newShipsArray: ShipDetails[]) => {
          if (newShipsArray.length === 0) {
            return;
          }
          setRawShips(newShipsArray);
        },
      );
    }, generalConfig.shipsRefreshMs);
    return () => {
      clearInterval(intervalId);
    };
  }, []);

  // Configure the state and the WebSocket connection with the backend server
  const sortedShips = ShipService.sortList(rawShips, "desc");

  // Put filter threshold as a state
  const [filterThreshold, setFilterThreshold] = useState<number>(0);

  // Create a separate array for displayed ships
  const displayedShips = sortedShips.filter(
    (x) => x.anomalyScore >= filterThreshold,
  );

  // Return the main view of the application
  return (
    <div className="App" id="root-div">
      <LMap
        ships={displayedShips}
        refObjects={extractedFunctionsSide}
        ref={extractedFunctionsMap}
      />
      <Side
        ships={displayedShips}
        mapCenteringFun={mapCenteringFun}
        setFilterThreshold={setFilterThreshold}
        anomalyThreshold={filterThreshold}
        ref={extractedFunctionsSide}
        extractedFunctionsMap={extractedFunctionsMap}
      />
    </div>
  );
}

export default App;
