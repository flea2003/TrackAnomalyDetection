import L from "leaflet";
import React from "react";
import mapConfig from "../../configs/mapConfig.json";
import ShipDetails from "../../model/ShipDetails";
import { ShipIconDetailsType } from "./ShipIconDetails";
import { PageChangerRef } from "../Side/Side";
import { createShipIcon, handleMouseOutShipIcon, handleMouseOverShipIcon } from "./ShipIcon";

/**
 *
 * @param map
 * @param clusterLayer
 */
export function initializeMarkerClusterLayer(map: L.Map, clusterLayer: React.MutableRefObject<L.MarkerClusterGroup | null>) {
  const markers = L.markerClusterGroup({
    chunkedLoading: mapConfig.clusterChunkedLoading,
    chunkInterval: mapConfig.clusterChunkInterval,
    chunkDelay: mapConfig.clusterChunkDelay
  });
  map.addLayer(markers);
  clusterLayer.current = markers;
}

export function getInitialShipIconsDetailsInfo() {
  return {
    show: false,
    x: 0,
    y: 0,
    shipDetails: null
  } as ShipIconDetailsType;
}

/**
 *
 * @param ships
 * @param map
 * @param setHoverInfo
 * @param pageChangerRef
 */
export async function getMarkersForAllShips(ships: ShipDetails[], map: L.Map,
                                            setHoverInfo: (value: (((prevState: ShipIconDetailsType) => ShipIconDetailsType) | ShipIconDetailsType)) => void,
                                            pageChangerRef: React.RefObject<PageChangerRef>) {
  return ships.map(
    ship => getMarker(ship, map, setHoverInfo, pageChangerRef)
  );
}

/**
 *
 * @param ship
 * @param map
 * @param setHoverInfo
 * @param pageChangerRef
 */
function getMarker(ship: ShipDetails, map: L.Map,
                   setHoverInfo: (value: (((prevState: ShipIconDetailsType) => ShipIconDetailsType) | ShipIconDetailsType)) => void,
                   pageChangerRef: React.RefObject<PageChangerRef>) {

  return L.marker([ship.lat, ship.lng], {
    icon: createShipIcon(
      ship.anomalyScore / 100,
      ship.heading,
      ship.speed > 0
    )
  })
    .bindPopup("ID: " + ship.id)
    .on("click", (e) => {
      map.flyTo(e.latlng, Math.max(map.getZoom(), 4));
      handleMouseOutShipIcon(setHoverInfo);
      if (pageChangerRef.current !== null) {
        pageChangerRef.current.pageChanger({
          currentPage: "objectDetails",
          shownItemId: ship.id
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