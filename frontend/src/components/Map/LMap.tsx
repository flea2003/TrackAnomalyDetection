import React, {
  forwardRef,
  useEffect,
  useImperativeHandle,
  useRef,
  useState,
} from "react";
import ShipDetails from "../../model/ShipDetails";
import ShipIconDetails, { ShipIconDetailsType } from "./ShipIconDetails";
import { PageChangerRef } from "../Side/Side";
import ErrorNotificationService from "../../services/ErrorNotificationService";
import {
  updateMarkersForShips,
  getInitialShipIconsDetailsInfo,
  getMarkersClustersLayer,
} from "./ShipMarkerCluster";
import L from "leaflet";
import "leaflet.markercluster";

import "../../styles/map.css";
import "../../styles/common.css";
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
    // Map is ref to have one instance. This ref will be initialized in useEffect.
    const mapRef = useRef<L.Map | null>(null);

    // The ref to layer which handles the clustering logic (using Leaflet.marketcluster library)
    const markersClustersRef = useRef<L.MarkerClusterGroup | null>(null);

    // Define the methods that will be reachable from the parent
    useImperativeHandle(ref, () => ({
      centerMapOntoShip: (ship: ShipDetails) =>
        mapFlyToShip(mapRef, ship, ships),
    }));

    // Initialize the hoverInfo variable that will manage the display of the
    // pop-up div containing reduced information about a particular ship
    const [hoverInfo, setHoverInfo] = useState<ShipIconDetailsType>(
      getInitialShipIconsDetailsInfo(),
    );

    // Initialize map (once).
    useEffect(() => {
      if (mapRef.current !== null) return;
      initializeMap(mapRef, markersClustersRef);
    }, []);

    // Update the ship markers when the ships array changes.
    useEffect(() => {
      const map = mapRef.current;
      if (!map) return;

      updateMarkersForShips(
        ships,
        map,
        setHoverInfo,
        pageChangerRef,
        markersClustersRef,
      );
    }, [pageChangerRef, ships]);

    return constructMapContainer(hoverInfo);
  },
);

/**
 * Constructs React JSX element which stores the Leaflet map.
 * IDs `map-container` and `map` are assigned to divs, so that the Leaflet library
 * automatically finds them and knows where to render the map.
 *
 * @param hoverInfo hover information to show
 */
function constructMapContainer(hoverInfo: ShipIconDetailsType) {
  return (
    <div id="map-container">
      <div id="map" data-testid="map"></div>
      {hoverInfo.show && hoverInfo.shipDetails !== null && (
        <div>
          <ShipIconDetails {...hoverInfo}></ShipIconDetails>
        </div>
      )}
    </div>
  );
}

/**
 * Centers the map to the given ship. This is done using Leaflet's `flyTo` method,
 * which provides a nice animation.
 *
 * If the map is not initialized yet, or the given ship is undefined or not found,
 * the notification is added, and nothing is done with centering the map.
 *
 * @param mapRef the reference to a map
 * @param ship the ship to which the map will be centered to
 * @param ships the array of all current ships
 */
function mapFlyToShip(
  mapRef: React.MutableRefObject<L.Map | null>,
  ship: ShipDetails | null | undefined,
  ships: ShipDetails[],
) {
  if (mapRef.current == null) {
    ErrorNotificationService.addWarning(
      "map is not initialized in the call centerMapOntoShip",
    );
    return;
  }

  // Check if requested ship (still) exists
  if (
    ship === null ||
    ship === undefined ||
    !ships.some((x) => x.id === ship.id)
  ) {
    ErrorNotificationService.addWarning(
      "Required ship does not exist while trying to center map",
    );
    return;
  }

  const map = mapRef.current as L.Map;
  map.flyTo([ship.lat, ship.lng], mapConfig.centeringShipZoomLevel, {
    animate: true,
    duration: mapConfig.centeringShipTransitionTime,
  });
}

/**
 * Initializes the map. The passed map and markers clusters layer references are assigned,
 * so that they could be used later.
 *
 * @param mapRef the reference to the Leaflet map
 * @param markersClustersRef the reference to the Leaflet Marker Cluster Group (from additional library)
 */
function initializeMap(
  mapRef: React.MutableRefObject<L.Map | null>,
  markersClustersRef: React.MutableRefObject<L.MarkerClusterGroup | null>,
) {
  const tileLayer = getTileLayer();
  const markersClustersLayer = getMarkersClustersLayer();

  markersClustersRef.current = markersClustersLayer;
  mapRef.current = L.map("map", {
    minZoom: 2,
    maxZoom: 17,
    center: [47.0105, 28.8638],
    zoom: 8,
    maxBounds: getMapGlobalBounds(),
    maxBoundsViscosity: 0, // the map global bounds are hard (user cannot drag out of them)
    layers: [tileLayer, markersClustersLayer],
  });
}

/**
 * Creates and returns the tile layer used by the map.
 */
function getTileLayer() {
  return L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution:
      '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
  });
}

/**
 * Returns the `LatLngBounds` object representing the bounds of the map.
 * The user will not be able to move the map outside these bounds.
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
