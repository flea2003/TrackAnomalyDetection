import L from "leaflet";
import React from "react";
import mapConfig from "../../configs/mapConfig.json";
import ShipDetails from "../../model/ShipDetails";
import { PageChangerRef } from "../Side/Side";
import {
  createShipIcon,
  handleMouseOutShipIcon,
  handleMouseOverShipIcon,
} from "./ShipIcon";
import { ShipIconDetailsType } from "./ShipIconDetails";
import { calculateAnomalyColor } from "../../utils/AnomalyColorCalculator";

/**
 * ShipMarker class is used instead of the default Leaflet marker. This allows to also
 * save ship data to later be able to retrieve it (useful for cluster color calculation).
 */
class ShipMarker extends L.Marker {
  ship: ShipDetails;

  constructor(ship: ShipDetails) {
    const icon = createShipIcon(
      ship.anomalyScore / 100,
      ship.heading === 511 ? ship.course : ship.heading,
      ship.speed > 0,
    );
    super([ship.lat, ship.lng], { icon });

    this.ship = ship;
  }
}

/**
 * Icon creation function for the cluster. Creates a circular icon with the background color
 * representing the maximum anomaly among the ships in this cluster.
 */
function getClusterIcon(cluster: L.MarkerCluster) {
  const anomalyScores = cluster
    .getAllChildMarkers()
    .map((marker) => (marker as ShipMarker).ship.anomalyScore);

  const maxAnomalyScore = Math.max(...anomalyScores);
  const iconText = maxAnomalyScore + "%";

  const color = calculateAnomalyColor(maxAnomalyScore, false);
  const style = `background-color: ${color};`;

  return new L.DivIcon({
    html: `<div style="${style}"><span>${iconText}<span aria-label="markers"></span></span></div>`,
    className: "ship-marker-cluster",
    iconSize: new L.Point(40, 40),
  });
}

/**
 * Creates and returns the layer for clustering the markers. This layer is introduced by
 * the additional library "Leaflet Market Cluster Group".
 *
 * The markers that will be added to this layer are automatically clustered and rendered
 * on the map.
 */
export function getMarkersClustersLayer() {
  return L.markerClusterGroup({
    maxClusterRadius: mapConfig.clusterMaxRadius,
    iconCreateFunction: (cluster) => getClusterIcon(cluster),

    // Chunked loading settings
    chunkedLoading: mapConfig.clusterChunkedLoading,
    chunkInterval: mapConfig.clusterChunkInterval,
    chunkDelay: mapConfig.clusterChunkDelay,
  });
}

/**
 * Returns the array of the markers (Leaflet layers) for each of the ships.
 *
 * @param ships the array of current ships
 * @param map Leaflet map
 * @param setHoverInfo the function to change the state of the hover info object
 * @param pageChangerRef the reference to the function that allows to change pages
 * @param trackShipFunc function to set tracked ship
 */
async function getMarkersForAllShips(
  ships: ShipDetails[],
  map: L.Map,
  setHoverInfo: (
    value:
      | ((prevState: ShipIconDetailsType) => ShipIconDetailsType)
      | ShipIconDetailsType,
  ) => void,
  pageChangerRef: React.RefObject<PageChangerRef>,
  trackShipFunc: (ship: ShipDetails, zoomLevel: number) => void,
) {
  return ships.map((ship) =>
    getMarker(ship, map, setHoverInfo, pageChangerRef, trackShipFunc),
  );
}

/**
 * Constructs and returns the marker for an individual ship.
 * Custom icon is created, popup added, and the functionality for working with hover info.
 *
 * @param ship the ship for which a marker is created
 * @param map Leaflet map
 * @param setHoverInfo the function to change the state of the hover info object
 * @param pageChangerRef the reference to the function that allows to change pages
 * @param trackShipFunc function to set tracked ship
 */
function getMarker(
  ship: ShipDetails,
  map: L.Map,
  setHoverInfo: (
    value:
      | ((prevState: ShipIconDetailsType) => ShipIconDetailsType)
      | ShipIconDetailsType,
  ) => void,
  pageChangerRef: React.RefObject<PageChangerRef>,
  trackShipFunc: (ship: ShipDetails, zoomLevel: number) => void,
) {
  const onClickFunc = () => {
    trackShipFunc(ship, map.getZoom());
    handleMouseOutShipIcon(setHoverInfo);
    if (pageChangerRef.current !== null) {
      pageChangerRef.current.pageChanger({
        currentPage: "objectDetails",
        shownItemId: ship.id,
      });
    }
  };

  return new ShipMarker(ship)
    .on("click", onClickFunc)
    .on("mouseover", (e) => {
      handleMouseOverShipIcon(e, ship, map, setHoverInfo);
    })
    .on("mouseout", () => {
      handleMouseOutShipIcon(setHoverInfo);
    });
}

/**
 * Clears the previous ship markers, and adds new ones for the current ships.
 * This is done by calling async function that creates the markers, and then these
 * created markers are added to markers clusters layer in batch (using function `addLayers`).
 *
 * If `doFilteringBeforeDisplaying` is true, only the ships that are inside the current map
 * viewport (map bounds) are added. If `maxShipsOnScreen` is not -1, then only the top `maxShipOnScreen`
 * ships are added.
 *
 * @param ships the array of current ships
 * @param doFilteringBeforeDisplaying whether to do additional filtering before adding the markers
 * @param maxShipsOnScreen maximum number of ships to allow while filtering
 * @param map Leaflet map
 * @param setHoverInfo the function to change the state of the hover info object
 * @param pageChangerRef the reference to the function that allows to change pages
 * @param markersClustersRef the reference to the market clusters layer
 * @param trackShipFunc function to set tracked ship
 */
export function updateMarkersForShips(
  ships: ShipDetails[],
  doFilteringBeforeDisplaying: boolean,
  maxShipsOnScreen: number,
  map: L.Map,
  setHoverInfo: (
    value:
      | ((prevState: ShipIconDetailsType) => ShipIconDetailsType)
      | ShipIconDetailsType,
  ) => void,
  pageChangerRef: React.RefObject<PageChangerRef>,
  markersClustersRef: React.MutableRefObject<L.MarkerClusterGroup | null>,
  trackShipFunc: (ship: ShipDetails, zoomLevel: number) => void,
) {
  const shipsToRender = doFilteringBeforeDisplaying
    ? filterShips(ships, map, maxShipsOnScreen)
    : ships;

  getMarkersForAllShips(
    shipsToRender,
    map,
    setHoverInfo,
    pageChangerRef,
    trackShipFunc,
  ).then(async (lToAdd) => {
    const markersClustersLayer = markersClustersRef.current;
    if (markersClustersLayer === null) return;
    markersClustersLayer.clearLayers();
    markersClustersLayer.addLayers(lToAdd);
  });
}

/**
 * Filters the ships so that only the ones that are on screen are taken into account.
 * Also, only the top `maxShipsOnScreen` are taken. If `maxShipsOnScreen` is equal
 * to -1, then all ships are taken.
 *
 * This method assumes that the given ship array is already sorted based on the anomaly
 * score (this is indeed done in `App.tsx`).
 *
 * @param ships the array of current ships to filter before rendering
 * @param map the Leaflet map
 * @param maxShipsOnScreen maximum number of ships to display on screen
 */
function filterShips(
  ships: ShipDetails[],
  map: L.Map,
  maxShipsOnScreen: number,
) {
  const shipCount = maxShipsOnScreen === -1 ? ships.length : maxShipsOnScreen;

  return ships.filter((ship) => isShipInsideMap(ship, map)).slice(0, shipCount);
}

/**
 * Checks if the given ship is inside the current viewport of the Leaflet map.
 *
 * @param ship the ship to check
 * @param map the Leaflet map
 */
function isShipInsideMap(ship: ShipDetails, map: L.Map) {
  return map.getBounds().contains([ship.lat, ship.lng]);
}
