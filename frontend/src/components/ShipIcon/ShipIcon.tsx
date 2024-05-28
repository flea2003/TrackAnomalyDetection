import React from "react";
import L from "leaflet";
import { calculateAnomalyColor } from "../../utils/AnomalyColorCalculator";
import ShipDetails from "../../model/ShipDetails";
import { ShipIconDetailsType } from "../ShipIconDetails/ShipIconDetails";

/**
 * This function creates the ship icon using some magic and HTML canvas
 * @param weight - the color which the ship is going to take, where 0 is green and 1 red
 * @param rotation - how many degrees the ship must be rotated(heading value)
 * @return - the icon of the ship
 */
function createShipIcon(weight: number, rotation: number, isMoving: boolean): L.Icon {
  const color = calculateAnomalyColor(weight * 100);

  const canvas = document.createElement("canvas");
  const context = canvas.getContext("2d");

  if (!context) {
    throw new Error("Could not create 2D context.");
  }

  if (isMoving) {
    drawShipMovingArrow(color, rotation, canvas, context);
  } else {
    drawShipStationaryCircle(color, canvas, context);
  }

  return L.icon({
    iconUrl: canvas.toDataURL(),
    iconSize: [canvas.width, canvas.height], // Use canvas size as icon size
    iconAnchor: [canvas.width / 2, canvas.height / 2], // Anchor at the center
  });
}

function drawShipMovingArrow(color: string, rotation: number, canvas: HTMLCanvasElement, context: CanvasRenderingContext2D) {
  const size = 20;

  canvas.width = size;
  canvas.height = size;

  context.translate(10, 10);
  context.rotate((rotation * Math.PI) / 180);
  context.translate(-10, -10);
  const path = new Path2D();
  path.moveTo(size / 2, 0);
  path.lineTo(size / 6, size);
  path.lineTo(size / 2, (4 * size) / 5);
  path.lineTo((5 * size) / 6, size);
  path.lineTo(size / 2, 0);

  context.fillStyle = color;
  context.fill(path);

  context.lineWidth = 0.5;
  context.strokeStyle = "black";
  context.stroke(path);
}

function drawShipStationaryCircle(color: string, canvas: HTMLCanvasElement, context: CanvasRenderingContext2D) {
  const size = 20;
  const radius = 7;

  canvas.width = size;
  canvas.height = size;

  const path = new Path2D();
  path.arc(size / 2, size / 2, radius, 0, 2 * Math.PI);

  context.fillStyle = color;
  context.fill(path);

  context.lineWidth = 0.5;
  context.strokeStyle = "black";
  context.stroke(path);
}

/**
 * Utility function to extract the coordinates of an event
 * @param e - event triggered by a user action
 * @param map - L.Map instance
 */
const extractIconCoordinates = (e: L.LeafletMouseEvent, map: L.Map) => {
  // Extract the latitude and longitude coordinates from the LeafletMouseEvent
  const latLongCoords = e.latlng;
  const containerPort = map.latLngToContainerPoint(latLongCoords);
  const { x, y } = containerPort;
  return { x, y };
};

/**
 * Utility function to update the state of the information pop-up div
 * whenever the mouse hovers on a ship icon
 * @param e - the event triggered by the "mouseover" action
 * @param ship - corresponding ShipDetails instance
 * @param map - reference to the L.Map instance
 * @param setHoverInfo - state management function
 */
const handleMouseOverShipIcon = (
  e: L.LeafletMouseEvent,
  ship: ShipDetails,
  map: L.Map,
  setHoverInfo: React.Dispatch<React.SetStateAction<ShipIconDetailsType>>,
) => {
  const { x, y } = extractIconCoordinates(e, map);
  setHoverInfo({
    show: true,
    x: x,
    y: y,
    shipDetails: ship,
  } as ShipIconDetailsType);
};

/**
 * Utility function to update the state of the information pop-up div
 * whenever the mouse leaves the surface of the ship icon
 * @param e - the event triggered by the "mouseout" action
 * @param setHoverInfo - state management function
 */
const handleMouseOutShipIcon = (
  e: L.LeafletMouseEvent,
  setHoverInfo: React.Dispatch<React.SetStateAction<ShipIconDetailsType>>,
) => {
  setHoverInfo({
    show: false,
    x: 0,
    y: 0,
    shipDetails: null,
  } as ShipIconDetailsType);
};

export { createShipIcon, handleMouseOverShipIcon, handleMouseOutShipIcon };
