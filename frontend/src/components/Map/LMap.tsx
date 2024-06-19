import React, {
  forwardRef,
  useEffect,
  useImperativeHandle,
  useRef,
  useState,
} from "react";
import ShipDetails, { differentShipPositions } from "../../model/ShipDetails";
import ShipIconDetails, { ShipIconDetailsType } from "./ShipIconDetails";
import { ExtractedFunctionsSide } from "../Side/Side";
import ErrorNotificationService from "../../services/ErrorNotificationService";
import {
  getMarkersClustersLayer,
  updateMarkersForShips,
} from "./ShipMarkerCluster";
import L, { LatLng } from "leaflet";
import "leaflet.markercluster";
import { calculateAnomalyColor } from "../../utils/AnomalyColorCalculator";
import mapConfig from "../../configs/mapConfig.json";
import TrajectoryPoint from "../../model/TrajectoryPoint";
import TrajectoryService from "../../services/TrajectoryService";
import { CurrentPage } from "../../App";

import "../../styles/map.css";
import "../../styles/common.css";
import "leaflet.markercluster/dist/MarkerCluster.css";
import "leaflet.markercluster/dist/MarkerCluster.Default.css";
import "leaflet/dist/leaflet.css";
import TrajectoryAndNotificationPair from "../../model/TrajectoryAndNotificationPair";

interface MapProps {
  ships: ShipDetails[];
  displayedTrajectoryAndNotifications: TrajectoryAndNotificationPair;
  setDisplayedTrajectory: React.Dispatch<
    React.SetStateAction<TrajectoryAndNotificationPair>
  >;
  refObjects: React.RefObject<ExtractedFunctionsSide>;
  currentPage: CurrentPage;
}

// Define the type of the ref object
interface ExtractedFunctionsMap {
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
 * @param currentPage current page that is being displayed
 * @param pageChanger function that, when called, changes the page displayed in the second column.
 */
const LMap = forwardRef<ExtractedFunctionsMap, MapProps>(
  (
    {
      ships,
      displayedTrajectoryAndNotifications,
      setDisplayedTrajectory,
      refObjects,
      currentPage
    },
    ref,
  ) => {
    // Map is ref to have one instance. This ref will be initialized in useEffect.
    const mapRef = useRef<L.Map | null>(null);

    // The ref to layer which handles the clustering logic (using Leaflet.markercluster library)
    const markersClustersRef = useRef<L.MarkerClusterGroup | null>(null);

    // Initialize the state for tracked ship
    const [trackedShip, setTrackedShip] = useState(getDefaultTrackedShipInfo());

    // Initialize the hoverInfo variable that will manage the display of the
    // pop-up div containing reduced information about a particular ship
    const [hoverInfo, setHoverInfo] = useState(getDefaultHoverInfo());

    // Create a notification const for easier use. It stores reference to all notifications
    const notifications = refObjects.current?.notifications;

    const trackShip = (ship: ShipDetails, zoomLevel: number) => {
      const newTrackedShip = { ship, zoomLevel } as TrackedShipType;
      mapFlyToShip(mapRef, newTrackedShip);
      setTrackedShip(newTrackedShip);
    };

    // Define the methods that will be reachable by the parent components.
    useImperativeHandle(ref, () => ({
      centerMapOntoShip: (ship: ShipDetails) =>
        trackShip(ship, mapConfig.centeringShipZoomLevel),
    }));

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
      const map = mapRef.current;
      if (!map) return;

      // Update centering on the tracked ship
      const ship = trackedShip.ship;
      const shipInList = ships.find(
        (s) => ship !== null && ship !== undefined && s.id === ship.id,
      );
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
          refObjects,
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
    }, [refObjects, ships, trackedShip]);

    /**
     * Checks if the trajectory on the map needs to be updated, based on the
     * displayed page and current ship array information
     *
     * If trajectory need to be updated, queries the needed data, and sets the
     * trajectory state (which then visually updates the map)
     */
    useEffect(() => {
      // If currently object details are displayed, a trajectory of a corresponding ship must
      // be present on a map.
      if (currentPage.currentPage === "objectDetails") {
        // Find a needed ship from the array of all ships
        const ship = ships.find((x) => x.id === currentPage.shownItemId);

        // Check if the trajectory should be updated in the map. If it has to be updated,
        // update the trajectory state by querying the needed data from the backend
        if (TrajectoryService.shouldQueryBackend(ship))
          TrajectoryService.queryBackendForSampledHistoryOfAShip(
            currentPage.shownItemId,
          ).then((trajectory) =>
            setDisplayedTrajectory(
              new TrajectoryAndNotificationPair(trajectory, undefined),
            ),
          );
      }

      // If currently notification details are displayed, a trajectory of a corresponding ship must
      // be present on a map, and ALSO, a marker where the notification took place must appear.
      else if (currentPage.currentPage === "notificationDetails") {
        // Check if notifications array is defined
        if (notifications === undefined) {
          setDisplayedTrajectory(
            new TrajectoryAndNotificationPair([], undefined),
          );
          return;
        }

        // Find the needed notification from the array
        const notification = notifications.find(
          (x) => x.id === currentPage.shownItemId,
        );

        // TODO: ACTUALLY, THIS CAN HAPPEN, AS WE ONLY STORE LIKE 1000 NOTIFICATIONS IN THE FRONTEND.
        //  PERHAPS IN THIS CASE, BACKEND SHOULD BE QUERIED TO RETRIEVE THE NOTIFICATION. However, we won't cosider this now.
        if (notification === undefined) {
          setDisplayedTrajectory(
            new TrajectoryAndNotificationPair([], undefined),
          );
          return;
        }

        // Compute the object for the notification marker that will be displayed
        const notificationLat = notification.shipDetails.lat;
        const notificationLng = notification.shipDetails.lng;
        const notificationLatLng = new LatLng(notificationLat, notificationLng);

        // Find the ship in the ships array
        const ship = ships.find((x) => x.id === notification.shipDetails.id);

        // Check if the trajectory should be updated in the map. If it has to be updated,
        // update the trajectory state by querying the needed data from the backend
        if (TrajectoryService.shouldQueryBackend(ship))
          TrajectoryService.queryBackendForSampledHistoryOfAShip(
            notification.shipDetails.id,
          ).then((trajectory) => {
            setDisplayedTrajectory(
              new TrajectoryAndNotificationPair(trajectory, notificationLatLng),
            );
          });
      }

      // In case any other page is displayed, get rid of the displayed trajectory
      else {
        setDisplayedTrajectory(
          new TrajectoryAndNotificationPair([], undefined),
        );
      }
    }, [currentPage, notifications, ships, setDisplayedTrajectory]);

    /**
     * Updates trajectory visually on the map
     */
    useEffect(() => {
      const map = mapRef.current;
      if (map == null) return;

      const displayedTrajectory =
        displayedTrajectoryAndNotifications.trajectory;
      const notificationCoordinates =
        displayedTrajectoryAndNotifications.notification;

      if (displayedTrajectory.length === 0) return;

      // Initialize a list for storing the references of added trajectory layers,
      // so that later we can remove them from the map
      const tempLayers: L.Layer[] = [];

      // Display initial point marker on the map
      addInitialPointMarkerOnTheMap(displayedTrajectory, map, tempLayers);

      // Display the colored trajectory on the map
      addColoredTrajectoryOnTheMap(displayedTrajectory, map, tempLayers);

      // In case a notification was clicked, add a notification marker
      if (notificationCoordinates !== undefined)
        addNotificationMarkersOnTheMap(
          notificationCoordinates,
          map,
          tempLayers,
        );

      // Once the state changes, remove the trajectory from the map
      return () => {
        tempLayers.forEach((x) => {
          if (map !== null) map.removeLayer(x);
        });
      };
    }, [displayedTrajectoryAndNotifications]);

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

  if (
    map === null ||
    ship === null ||
    ship === undefined ||
    ship.lat === undefined ||
    ship.lng === undefined
  ) {
    ErrorNotificationService.addWarning(
      "Cannot center the map on the ship: map, ship or its position is null",
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

/**
 * Method that displays the colored trajectory on the map. It does it by creating
 * many small components of the L.polyline objects (from consecutive AIS signals),
 * each with a needed color.
 *
 * @param displayedTrajectory displayed trajectory array
 * @param map map that is displayed
 * @param tempLayers layers that are being added to the map
 */
function addColoredTrajectoryOnTheMap(
  displayedTrajectory: TrajectoryPoint[],
  map: L.Map,
  tempLayers: L.Layer[],
) {
  // Add colored trajectory as a composition of all parts in the coordinates array
  for (let i = 0; i < displayedTrajectory.length - 1; i++) {
    const point1 = displayedTrajectory[i];
    const point2 = displayedTrajectory[i + 1];

    const polyline = L.polyline(
      [
        [point1.latitude, point1.longitude],
        [point2.latitude, point2.longitude],
      ],
      {
        color: calculateAnomalyColor(point2.anomalyScore, true),
        opacity: 0.6,
        weight: 3,
      },
    );

    polyline.addTo(map);
    tempLayers.push(polyline);
  }
}

/**
 * Method that displays an initial point of the trajectory as a blue circle marker
 * on the map
 *
 * @param displayedTrajectory displayed trajectory array
 * @param map map that is displayed
 * @param tempLayers layers that are being added to the map
 */
function addInitialPointMarkerOnTheMap(
  displayedTrajectory: TrajectoryPoint[],
  map: L.Map,
  tempLayers: L.Layer[],
) {
  // Create a blue circle for the initial coordinate of the ship
  const initialMarker = L.circleMarker(
    [
      displayedTrajectory[displayedTrajectory.length - 1].latitude,
      displayedTrajectory[displayedTrajectory.length - 1].longitude,
    ],
    {
      radius: 7,
      color: "0000ff",
      fillColor: "#0000ff",
      fillOpacity: 0.5,
    },
  );

  // Add that circle to the map, and also the layers reference list
  map.addLayer(initialMarker);
  tempLayers.push(initialMarker);
}

/**
 * Method that displays orange markers on the map that correspond to the notifications that
 * for that trajectory.
 *
 * @param notificationCoordinates coordinates for notifications that have to be displayed
 * @param map map that is displayed
 * @param tempLayers layers that are being added to the map
 */
function addNotificationMarkersOnTheMap(
  notificationCoordinates: LatLng,
  map: L.Map,
  tempLayers: L.Layer[],
) {
  const circleMarker = L.circleMarker(
    [notificationCoordinates.lat, notificationCoordinates.lng],
    {
      radius: 7,
      color: "orange",
      fillColor: "orange",
      fillOpacity: 1,
    },
  );

  circleMarker.addTo(map);
  tempLayers.push(circleMarker);
}

// Needed for Lint to work (React itself does not require this)
LMap.displayName = "Map";

export default LMap;
export type { ExtractedFunctionsMap };
