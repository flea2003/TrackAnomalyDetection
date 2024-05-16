import React, {
  useEffect,
  useState,
  forwardRef,
  useImperativeHandle,
} from "react";
import L from "leaflet";
import createShipIcon from "../ShipIcon/ShipIcon";
import "../../styles/map.css";
import "../../styles/common.css";

import { CurrentPage } from "../../App";
import ShipDetails from "../../model/ShipDetails";

import mapStyleConfig from "../../styles/mapConfig.json";

interface MapProps {
  ships: ShipDetails[];
  pageChanger: (currentPage: CurrentPage) => void;
}

/**
 * This function creates a Leaflet map with the initial settings. It is called only once, when the component is mounted.
 * @returns the created map
 */
function getInitialMap() {
  const initialMap = L.map("map", {
    minZoom: 2,
    maxZoom: 17,
  }).setView([47.0105, 28.8638], 8);

  const southWest = L.latLng(-90, -180);
  const northEast = L.latLng(90, 180);
  const bounds = L.latLngBounds(southWest, northEast);

  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution:
      '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
  }).addTo(initialMap);

  initialMap.setMaxBounds(bounds);
  initialMap.on("drag", function () {
    initialMap.panInsideBounds(bounds, { animate: false });
  });

  return initialMap;
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
const Map = forwardRef<MapExportedMethodsType, MapProps>(
  ({ ships, pageChanger }, ref) => {
    // Initialize the map as state, since we want to have a single instance
    const [map, setMap] = useState<L.Map | null>(null);

    // Define the methods that will be reachable from the parent
    useImperativeHandle(ref, () => ({
      centerMapOntoShip(ship: ShipDetails) {
        if (map == null) {
          return;
        }
        map.flyTo(
          [ship.lat, ship.lng],
          mapStyleConfig["zoom-level-when-clicked-on-ship-in-list"],
          {
            animate: true,
            duration: mapStyleConfig["transition-time"],
          },
        );
      },
    }));

    // Everything to do with the map updates should be done inside useEffect
    useEffect(() => {
      // If the map is null, we need to create it. We do it once, with state
      if (map == null) {
        const initialMap = getInitialMap();
        setMap(initialMap);
      }

      // If not yet created, do not do anything, just wait
      if (map == null) {
        return;
      }

      // Add all ship icons to the map
      ships.forEach((ship) => {
        L.marker([ship.lat, ship.lng], {
          icon: createShipIcon(ship.anomalyScore / 100, ship.heading),
        })
          .addTo(map)
          .bindPopup(ship.id)
          .on("click", (e) => {
            map.setView(e.latlng, map.getZoom());
            pageChanger({ currentPage: "objectDetails", shownShipId: ship.id });
          });
      });

      return () => {
        if (map) {
          map.eachLayer(function (layer: L.Layer) {
            if (layer instanceof L.Marker) {
              map.removeLayer(layer);
            }
          });
        }
      };
    }, [map, pageChanger, ships]);

    return (
      <div id="map-container">
        <div id="map" data-testid="map"></div>
      </div>
    );
  },
);

// Needed for Lint to work (React itself does not require this)
Map.displayName = "Map";

export default Map;
export type { MapExportedMethodsType };
