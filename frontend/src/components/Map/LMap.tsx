import React, { forwardRef, useEffect, useImperativeHandle, useRef, useState } from "react";
import L from "leaflet";
import ShipDetails from "../../model/ShipDetails";

import "../../styles/map.css";
import "../../styles/common.css";
import ShipIconDetails, { ShipIconDetailsType } from "./ShipIconDetails";
import { PageChangerRef } from "../Side/Side";
import "leaflet.markercluster";
import { MapContainer, TileLayer } from "react-leaflet";
import ErrorNotificationService from "../../services/ErrorNotificationService";
import {
  getInitialShipIconsDetailsInfo,
  getMarkersForAllShips,
  initializeMarkerClusterLayer
} from "./ShipMarkerCluster";

import "leaflet.markercluster/dist/MarkerCluster.css";
import "leaflet.markercluster/dist/MarkerCluster.Default.css";
import "leaflet/dist/leaflet.css";

import mapConfig from "../../configs/mapConfig.json";


interface MapProps {
  ships: ShipDetails[];
  pageChangerRef: React.RefObject<PageChangerRef>;
}

// Define the type of the ref object
interface MapExportedMethodsType {
  centerMapOntoShip: (details: ShipDetails) => void;
}

/**
 * This component is the first column of the main view of the application. It displays the map with all the ships.
 * A list of ships is passed as a prop.
 *
 * @param ships the ships to display on the map
 * @param pageChanger function that, when called, changes the page displayed in the second column.
 */
const LMap = forwardRef<MapExportedMethodsType, MapProps>(
  ({ ships, pageChangerRef }, ref) => {
    // Map is ref to have one instance. The ref will be assigned in Leaflet's MapContainer component.
    const mapRef = useRef(null);

    // The layer which handles the clustering logic (using Leaflet.marketcluster library)
    const clusterLayer= useRef<L.MarkerClusterGroup | null>(null);

    // Define the methods that will be reachable from the parent
    useImperativeHandle(ref, () => ({
      centerMapOntoShip: (ship: ShipDetails) => mapFlyToShip(mapRef, ship, ships)
    }));

    // Initialize the hoverInfo variable that will manage the display of the
    // pop-up div containing reduced information about a particular ship
    const [hoverInfo, setHoverInfo] = useState<ShipIconDetailsType>(getInitialShipIconsDetailsInfo());

    useEffect(() => {
      const map = mapRef.current as L.Map | null;
      if (!map) return;

      return updateMap(clusterLayer, map, ships, setHoverInfo, pageChangerRef);
    }, [pageChangerRef, ships]);


    return constructMapContainerComponent(mapRef, hoverInfo);
  },
);


function mapFlyToShip(mapRef: React.MutableRefObject<null>, ship: ShipDetails, ships: ShipDetails[]) {
  if (mapRef.current == null) {
    ErrorNotificationService.addWarning(
      "map is not initialized in the call centerMapOntoShip"
    );
    return;
  }

  // Check if requested ship (still) exists
  if (ship === null || ship === undefined || !ships.some((x) => x.id === ship.id)) {
    ErrorNotificationService.addWarning("Required ship does not exist while trying to center map");
    return;
  }

  const map = mapRef.current as L.Map;
  map.flyTo(
    [ship.lat, ship.lng],
    mapConfig.centeringShipZoomLevel,
    {
      animate: true,
      duration: mapConfig.centeringShipTransitionTime
    }
  );
}

// TODO JSdocs everywhere
/**
 *
 * @param clusterLayer
 * @param map
 * @param ships
 * @param setHoverInfo
 * @param pageChangerRef
 */
function updateMap(clusterLayer: React.MutableRefObject<L.MarkerClusterGroup | null>,
                   map: L.Map, ships: ShipDetails[],
                   setHoverInfo: (value: (((prevState: ShipIconDetailsType) => ShipIconDetailsType) | ShipIconDetailsType)) => void,
                   pageChangerRef: React.RefObject<PageChangerRef>) {
  if (clusterLayer.current === null) {
    initializeMarkerClusterLayer(map, clusterLayer);
  }

  const mapPanInsideBoundsFunc = () => map.panInsideBounds(getMapGlobalBounds(), { animate: false });
  map.on("drag", mapPanInsideBoundsFunc);

  getMarkersForAllShips(ships, map, setHoverInfo, pageChangerRef).then(async lToAdd => {
    if (clusterLayer.current == null) return;
    clusterLayer.current.clearLayers();
    clusterLayer.current.addLayers(lToAdd);
  });

  return () => {
    map.off("drag", mapPanInsideBoundsFunc);
  };
}

function constructMapContainerComponent(mapRef: React.MutableRefObject<null>, hoverInfo: ShipIconDetailsType) {
  return (
    <MapContainer id={"map-container"} zoom={8} center={[47.0105, 28.8638]} ref={mapRef}
                  minZoom={2} maxZoom={17} preferCanvas={true}
                  maxBounds={getMapGlobalBounds()}
    >

      <TileLayer url={"https://tile.openstreetmap.org/{z}/{x}/{y}.png"}
                 maxZoom={19}
                 attribution={"&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a>"}
      />
      {hoverInfo.show && hoverInfo.shipDetails !== null
        && (
          <div>
            <ShipIconDetails {...hoverInfo}></ShipIconDetails>
          </div>
        )}
    </MapContainer>
  );
}

/**
 *
 */
function getMapGlobalBounds() {
  const southWest = L.latLng(-90, -180);
  const northEast = L.latLng(90, 180);
  return L.latLngBounds(southWest, northEast);
}

// Needed for Lint to work (React itself does not require this)
LMap.displayName = "Map";

export default LMap;
export type { MapExportedMethodsType };
