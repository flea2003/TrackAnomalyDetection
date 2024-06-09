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

/**
 * Creates and returns the layer for clustering the markers. This layer is introduced by
 * the additional library "Leaflet Market Cluster Group".
 *
 * The markers that will be added to this layer are automatically clustered and rendered
 * on the map.
 */
export function getMarkersClustersLayer() {
  return L.markerClusterGroup({
    // Chunked loading settings
    chunkedLoading: mapConfig.clusterChunkedLoading,
    chunkInterval: mapConfig.clusterChunkInterval,
    chunkDelay: mapConfig.clusterChunkDelay,
  });
}

/**
 * The initial values for the `ShipIconsDetailsType` object.
 */
export function getInitialShipIconsDetailsInfo() {
  return {
    show: false,
    x: 0,
    y: 0,
    shipDetails: null,
  } as ShipIconDetailsType;
}

/**
 * Returns the array of the markers (Leaflet layers) for each of the ships.
 *
 * @param ships the array of current ships
 * @param map Leaflet map
 * @param setHoverInfo the function to change the state of the hover info object
 * @param pageChangerRef the reference to the function that allows to change pages
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
) {
  return ships.map((ship) =>
    getMarker(ship, map, setHoverInfo, pageChangerRef),
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
) {
  return L.marker([ship.lat, ship.lng], {
    icon: createShipIcon(ship.anomalyScore / 100, ship.heading, ship.speed > 0),
  })
    .bindPopup("ID: " + ship.id)
    .on("click", (e) => {
      map.flyTo(e.latlng, Math.max(map.getZoom(), 4));
      handleMouseOutShipIcon(setHoverInfo);
      if (pageChangerRef.current !== null) {
        pageChangerRef.current.pageChanger({
          currentPage: "objectDetails",
          shownItemId: ship.id,
        });
      }
    })
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
 * @param ships the array of current ships
 * @param map Leaflet map
 * @param setHoverInfo the function to change the state of the hover info object
 * @param pageChangerRef the reference to the function that allows to change pages
 * @param markersClustersRef the reference to the market clusters layer
 */
export function updateMarkersForShips(
  ships: ShipDetails[],
  map: L.Map,
  setHoverInfo: (
    value:
      | ((prevState: ShipIconDetailsType) => ShipIconDetailsType)
      | ShipIconDetailsType,
  ) => void,
  pageChangerRef: React.RefObject<PageChangerRef>,
  markersClustersRef: React.MutableRefObject<L.MarkerClusterGroup | null>,
) {
  getMarkersForAllShips(ships, map, setHoverInfo, pageChangerRef).then(
    async (lToAdd) => {
      const markersClustersLayer = markersClustersRef.current;
      if (markersClustersLayer === null) return;
      markersClustersLayer.clearLayers();
      markersClustersLayer.addLayers(lToAdd);
    },
  );
}
