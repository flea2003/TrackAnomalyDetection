import React, {
  forwardRef,
  useEffect,
  useImperativeHandle,
  useRef,
  useState,
} from "react";
import ShipDetails, { differentShipPositions } from "../../model/ShipDetails";
import ShipIconDetails, { ShipIconDetailsType } from "./ShipIconDetails";
import { PageChangerRef } from "../Side/Side";
import ErrorNotificationService from "../../services/ErrorNotificationService";
import {
  getMarkersClustersLayer,
  updateMarkersForShips,
} from "./ShipMarkerCluster";
import L, { LatLng } from "leaflet";
import "leaflet.markercluster";
import {getTrajectoriesLayer} from "./TrajectoriesLayer";

import "../../styles/map.css";
import "../../styles/common.css";
import "leaflet.markercluster/dist/MarkerCluster.css";
import "leaflet.markercluster/dist/MarkerCluster.Default.css";
import "leaflet/dist/leaflet.css";

import mapConfig from "../../configs/mapConfig.json";
import TrajectoryResponseItem from "../../templates/TrajectoryResponseItem";
import TrajectoryPoint from "../../model/TrajectoryPoint";

interface MapProps {
  ships: ShipDetails[];
  pageChangerRef: React.RefObject<PageChangerRef>;
  displayedTrajectory: TrajectoryPoint[];
}

// Define the type of the ref object
interface MapExportedMethodsType {
  centerMapOntoShip: (details: ShipDetails) => void;
}

interface TrackedShipType {
  ship: ShipDetails | null;
  zoomLevel: number;
}

/**
 * This component is the first column of the main view of the application. It displays the map with all the ships.
 * A list of ships is passed as a prop.
 *
 * @param ships the ships to display on the map
 * @param pageChanger function that, when called, changes the page displayed in the second column.
 */
const LMap = forwardRef<MapExportedMethodsType, MapProps>(
  ({ ships, pageChangerRef, displayedTrajectory }, ref) => {
    // Map is ref to have one instance. This ref will be initialized in useEffect.
    const mapRef = useRef<L.Map | null>(null);

    // The ref to layer which handles the clustering logic (using Leaflet.marketcluster library)
    const markersClustersRef = useRef<L.MarkerClusterGroup | null>(null);

    // Initialize the state for tracked ship
    const [trackedShip, setTrackedShip] = useState(getDefaultTrackedShipInfo());


    const [map, setMapState] = useState(mapRef.current);


    const trackShip = (ship: ShipDetails, zoomLevel: number) => {
      const newTrackedShip = { ship, zoomLevel } as TrackedShipType;
      mapFlyToShip(mapRef, newTrackedShip);
      setTrackedShip(newTrackedShip);
    };

    // Define the methods that will be reachable from the parent
    useImperativeHandle(ref, () => ({
      centerMapOntoShip: (ship: ShipDetails) =>
        trackShip(ship, mapConfig.centeringShipZoomLevel),
    }));

    // Initialize the hoverInfo variable that will manage the display of the
    // pop-up div containing reduced information about a particular ship
    const [hoverInfo, setHoverInfo] = useState(getDefaultHoverInfo());


    useEffect(() => {
      const sth = L.polyline(displayedTrajectory.map(x => new LatLng(x.latitude, x.longitude)));
      if (map == null) return;
      map.addLayer(sth);

    }, [displayedTrajectory]);

    // Initialize map (once).
    useEffect(() => {
      if (mapRef.current !== null) return;
      initializeMap(mapRef, markersClustersRef);
    }, []);

    // Update the ship markers when the ships array changes.
    // Also, add ability to update the markers when the user
    // is dragging through the map or zooming.
    // Finally, ship marker tracing functionality is implemented here.
    useEffect(() => {
      setMapState(mapRef.current);
      if (!map) return;

      // Update centering on the tracked ship
      const ship = trackedShip.ship;
      const shipInList = ships.find((s) => ship !== null && s.id === ship.id);
      if (
        shipInList !== undefined &&
        differentShipPositions(ship, shipInList)
      ) {
        trackShip(shipInList, trackedShip.zoomLevel);
      }

      const updateFunc = () => {
        updateMarkersForShips(
          ships,
          mapConfig.doFilteringBeforeDisplaying,
          mapConfig.maxShipsOnScreen,
          map,
          setHoverInfo,
          pageChangerRef,
          markersClustersRef,
          trackShip,
        );
      };
      // Same as function above, but only updates if the filtering is turned on
      const updateOnlyWhenFilterFunc = () => {
        if (mapConfig.doFilteringBeforeDisplaying) {
          updateFunc();
        }
      };

      // Function for removing centering the ship (used once the drag or zoom starts)
      const stopTracking = () => setTrackedShip(getDefaultTrackedShipInfo());

      // Function to reset the hover info
      const stopHoverInfo = () => setHoverInfo(getDefaultHoverInfo());

      updateFunc();

      map.on("moveend", updateOnlyWhenFilterFunc);
      map.on("movestart", stopTracking);
      map.on("movestart", stopHoverInfo);

      return () => {
        // Clear the effect
        map.off("moveend", updateOnlyWhenFilterFunc);
        map.off("movestart", stopTracking);
        map.off("movestart", stopHoverInfo);
      };
    }, [pageChangerRef, ships, trackedShip]);

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
 * Centers the map to the tracked ship. This is done using Leaflet's `flyTo` method,
 * which provides a nice animation.
 *
 * If the map is not initialized yet, or the tracked ship is null,
 * the notification is added, and nothing is done with centering the map.
 *
 * @param mapRef the reference to the Leaflet map
 * @param trackedShip the tracked ship and zoom level details
 */
function mapFlyToShip(
  mapRef: React.MutableRefObject<L.Map | null>,
  trackedShip: TrackedShipType,
) {
  const map = mapRef.current;
  const ship = trackedShip.ship;

  if (map === null || ship === null) {
    ErrorNotificationService.addWarning(
      "Cannot center the map on the ship: map or ship is null",
    );
    return;
  }

  map.flyTo([ship.lat, ship.lng], trackedShip.zoomLevel, {
    animate: true,
  });
}

/**
 * Constructs the default tracked ship info.
 * Used for setting tracked ship to be null (no ship is tracked).
 */
function getDefaultTrackedShipInfo() {
  return {
    ship: null,
    zoomLevel: 8,
  } as TrackedShipType;
}

/**
 * Constructs the default hover info field. Used when no hover should be shown.
 */
function getDefaultHoverInfo() {
  return {
    show: false,
    x: 0,
    y: 0,
    shipDetails: null,
  } as ShipIconDetailsType;
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
