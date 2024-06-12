import React from "react";
import { useState } from "react";
import LMap from "./components/Map/LMap";
import ShipDetails from "./model/ShipDetails";
import { MapExportedMethodsType } from "./components/Map/LMap";
import ErrorNotificationService from "./services/ErrorNotificationService";
import "./styles/common.css";
import Side, { PageChangerRef } from "./components/Side/Side";
import useWebSocketClient from "./utils/communication/WebSocketClient";
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
  const mapRef = React.useRef<MapExportedMethodsType>(null);
  const pageChangerRef = React.useRef<PageChangerRef>(null);

  // Create a function that passes a ship-centering function call to the map component
  const mapCenteringFun = (details: ShipDetails) => {
    if (mapRef.current !== null) {
      mapRef.current.centerMapOntoShip(details);
    } else {
      ErrorNotificationService.addWarning("mapRef is null");
    }
  };

  // Configure the state and the WebSocket connection with the backend server
  const allShips = ShipService.sortList(
    Array.from(useWebSocketClient().values()),
    "desc",
  );

  // Put filter threshold as a state
  const [filterThreshold, setFilterThreshold] = useState<number>(0);

  // Create a separate array for displayed ships
  const displayedShips = allShips.filter(
    (x) => x.anomalyScore >= filterThreshold,
  );

  // Return the main view of the application
  return (
    <div className="App" id="root-div">
      <LMap ships={displayedShips} pageChangerRef={pageChangerRef} ref={mapRef} />
      <Side
        ships={displayedShips}
        mapCenteringFun={mapCenteringFun}
        setFilterThreshold={setFilterThreshold}
        anomalyThreshold={filterThreshold}
        ref={pageChangerRef}
      />
    </div>
  );
}

export default App;
