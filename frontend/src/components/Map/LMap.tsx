import React, {
  useEffect,
  useState,
  forwardRef,
  useImperativeHandle,
} from "react";
import ErrorNotificationService from "../../services/ErrorNotificationService";
import L from "leaflet";
import {
  createShipIcon,
  handleMouseOutShipIcon,
  handleMouseOverShipIcon,
} from "../ShipIcon/ShipIcon";
import { CurrentPage } from "../../App";
import ShipDetails from "../../model/ShipDetails";
import mapStyleConfig from "../../configs/mapConfig.json";
import ShipIconDetails from "../ShipIconDetails/ShipIconDetails";
import { ShipIconDetailsType } from "../ShipIconDetails/ShipIconDetails";

import "../../styles/map.css";
import "../../styles/common.css";

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

interface MapProps {
  ships: ShipDetails[];
  pageChanger: (currentPage: CurrentPage) => void;
}

// Define the type of the ref object
interface MapExportedMethodsType {
  centerMapOntoShip: (details: ShipDetails) => void;
}

interface ShipIconTrackingType {
  x: number;
  y: number;
  shipId: number;
  isClicked: boolean;
  clickedFromList: boolean;
}

const defaultIconTrackingInfo = {
  x: 0,
  y: 0,
  shipId: -1,
  isClicked: false,
  clickedFromList: false,
} as ShipIconTrackingType;

const defaultHoverInfo = {
  show: false,
  x: 0,
  y: 0,
  shipDetails: null,
} as ShipIconDetailsType;

/**
 * This component is the first column of the main view of the application. It displays the map with all the ships.
 * A list of ships is passed as a prop.
 *
 * @param ships the ships to display on the map
 * @param pageChanger function that, when called, changes the page displayed in the second column.
 */
const LMap = forwardRef<MapExportedMethodsType, MapProps>(
  ({ ships, pageChanger }, ref) => {
    // Initialize the map as state, since we want to have a single instance
    const [map, setMap] = useState<L.Map | null>(null);

    // Define the methods that will be reachable from the parent
    useImperativeHandle(ref, () => ({
      centerMapOntoShip(ship: ShipDetails) {
        if (map == null) {
          ErrorNotificationService.addWarning(
            "map is null in the call centerMapOntoShip",
          );
          return;
        }

        // Check if the passed ship is even defined
        if (ship === undefined) return;
        if (ship.id === undefined) return;

        // Check if requested ship still exists
        if (ships.find((x) => x.id === ship.id) === undefined) return;

        // When icon centering is triggered from the entry list, zoom in on the ship icon
        trackShipIcon(ship, true);
      },
    }));

    // Initialize the hoverInfo variable that will manage the display of the
    // pop-up div containing reduced information about a particular ship
    const [hoverInfo, setHoverInfo] =
      useState<ShipIconDetailsType>(defaultHoverInfo);

    // Initialize the trackingInfo variable that will track the selected ship icon
    const [trackingInfo, setTrackingInfo] = useState<ShipIconTrackingType>(
      defaultIconTrackingInfo,
    );

    // Event-handling method which enables the tracking of a particular ship
    const trackShipIcon = (ship: ShipDetails, fromList: boolean) => {
      setTrackingInfo({
        x: ship.lng,
        y: ship.lat,
        shipId: ship.id,
        isClicked: true,
        clickedFromList: fromList,
      });
    };

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

      // When re-rendering the Map component, check if a ship icon is currently tracked
      if (trackingInfo.shipId !== -1) {
        const trackedShip = ships.find((x) => x.id === trackingInfo.shipId);
        if (trackedShip !== undefined) {
          if (
            trackingInfo.isClicked ||
            trackingInfo.x !== trackedShip.lng ||
            trackingInfo.y !== trackedShip.lat
          ) {
            if (trackingInfo.clickedFromList) {
              map.flyTo(
                [trackedShip.lat, trackedShip.lng],
                mapStyleConfig["zoom-level-when-clicked-on-ship-in-list"],
                {
                  animate: true,
                  duration: mapStyleConfig["transition-time"],
                },
              );
            } else {
              map.flyTo([trackedShip.lat, trackedShip.lng], map.getZoom(), {
                animate: true,
                duration: mapStyleConfig["transition-time"],
              });
            }
            setTrackingInfo({
              x: trackedShip.lng,
              y: trackedShip.lat,
              shipId: trackedShip.id,
              isClicked: false,
              clickedFromList: false,
            } as ShipIconTrackingType);
          }
        }
      }

      // Add all ship icons to the map
      ships.forEach((ship) => {
        try {
          L.marker([ship.lat, ship.lng], {
            icon: createShipIcon(
              ship.anomalyScore / 100,
              ship.heading === 511 ? ship.course : ship.heading,
              ship.speed > 0,
            ),
          })
            .addTo(map)
            .bindPopup("ID: " + ship.id)
            .on("click", (e) => {
              trackShipIcon(ship, false);
              handleMouseOutShipIcon(e, setHoverInfo);
              pageChanger({
                currentPage: "objectDetails",
                shownItemId: ship.id,
              });
            })
            .on("mouseover", (e) => {
              handleMouseOverShipIcon(e, ship, map, setHoverInfo);
            })
            .on("mouseout", (e) => {
              handleMouseOutShipIcon(e, setHoverInfo);
            });
        } catch (error) {
          if (error instanceof Error) {
            ErrorNotificationService.addWarning(
              "Error while adding an icon for ship with id " +
                ship.id +
                ": " +
                error.message,
            );
          }
        }
      });

      map
        .on("drag", () => {
          setTrackingInfo(defaultIconTrackingInfo);
        })
        .on("zoom", () => {
          setHoverInfo(defaultHoverInfo);
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
    }, [map, pageChanger, ships, trackingInfo]);

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
  },
);

// Needed for Lint to work (React itself does not require this)
LMap.displayName = "Map";

export default LMap;
export type { MapExportedMethodsType };
