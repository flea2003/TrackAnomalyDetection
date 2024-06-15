import React from "react";
import L from "leaflet";
import ShipService from "../../services/ShipService";
import ShipDetails from "../../model/ShipDetails";

class ShipTrajectory extends L.Path {
  ship: ShipDetails;

  constructor(ship: ShipDetails) {

    super();
    this.ship = ship;
  }
}

export function getTrajectoriesLayer() {
  return L.Polyline;
}

