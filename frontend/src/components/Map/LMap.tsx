import React, {
  useEffect,
  useState,
  forwardRef,
  useImperativeHandle, useRef
} from "react";
import ErrorNotificationService from "../../services/ErrorNotificationService";
import L, { LatLngBounds } from "leaflet";
import {
  createShipIcon,
  handleMouseOutShipIcon,
  handleMouseOverShipIcon,
} from "../ShipIcon/ShipIcon";
import { CurrentPage } from "../../App";
import ShipDetails from "../../model/ShipDetails";

import "../../styles/map.css";
import "../../styles/common.css";

import mapStyleConfig from "../../configs/mapConfig.json";
import ShipIconDetails from "../ShipIconDetails/ShipIconDetails";
import { ShipIconDetailsType } from "../ShipIconDetails/ShipIconDetails";
import { PageChangerRef } from "../Side/Side";
import useSupercluster from "use-supercluster";
import Supercluster, { ClusterProperties } from "supercluster";
import { Feature, GeoJsonProperties, Point } from "geojson";
import { MapContainer, TileLayer } from "react-leaflet";

/**
 * This function creates a Leaflet map with the initial settings. It is called only once, when the component is mounted.
 * @returns the created map
 */
// function getInitialMap(updateMarkersFunc: () => void) {
//   const initialMap = L.map("map", {
//     minZoom: 2,
//     maxZoom: 17,
//     preferCanvas: true
//   }).setView([47.0105, 28.8638], 8);
//
//   const southWest = L.latLng(-90, -180);
//   const northEast = L.latLng(90, 180);
//   const bounds = L.latLngBounds(southWest, northEast);
//
//   L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
//     maxZoom: 19,
//     attribution:
//       '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
//   }).addTo(initialMap);
//
//   initialMap.setMaxBounds(bounds);
//   initialMap.on("drag", function () {
//     initialMap.panInsideBounds(bounds, { animate: false });
//   });
//
//   initialMap.on("zoomend", () => {
//     console.log("zoomend");
//     updateMarkersFunc();
//   });
//   initialMap.on("moveend", updateMarkersFunc);
//
//   return initialMap;
// }

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
    // Initialize the map as state, since we want to have a single instance
    const [map, setMap] = useState<L.Map | null>(null);
    const mapRef = useRef(null);

    console.log("cia kazka print -Augustinas 2024")

    // Define the methods that will be reachable from the parent
    useImperativeHandle(ref, () => ({
      centerMapOntoShip(ship: ShipDetails) {
        if (map == null) {
          ErrorNotificationService.addWarning(
            "map is null in the call centerMapOntoShip",
          );
          return;
        }

        // Check if requested ship still exists
        if (ship === undefined) return;
        if (ships.find((x) => x.id === ship.id) === undefined) return;

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

    // Initialize the hoverInfo variable that will manage the display of the
    // pop-up div containing reduced information about a particular ship
    const [hoverInfo, setHoverInfo] = useState<ShipIconDetailsType>({
      show: false,
      x: 0,
      y: 0,
      shipDetails: null,
    } as ShipIconDetailsType);

    // which one to show
    const [showOld, setShowOld] = useState(false);

    useEffect(() => {
      function handleKeyDown(e: KeyboardEvent) {
        if (e.key.toLowerCase() === 'a') {
          setShowOld((o) => !o);
        }
      }

      document.addEventListener('keydown', handleKeyDown);

      return function cleanup() {
        document.removeEventListener('keydown', handleKeyDown);
      }
    }, []);

    // Everything to do with the map updates should be done inside useEffect
    useEffect(() => {
      const updateMapMarkers = () => {
        console.log("update map markers called");
        if (map == null) return;
        console.log("herererer");

        if (showOld) {
          console.log("showing old map");
          oldWayToaddIconsToMap(ships, map, setHoverInfo, pageChangerRef);
        } else {
          console.log("showing new map");
          addIconsToMap(ships, map, setHoverInfo, pageChangerRef);
        }
      }

      // If the map is null, we need to create it. We do it once, with state
      if (map == null) {
        console.log("creating initial map");
        // const initialMap = getInitialMap(updateMapMarkers);
        // setMap(initialMap);
      }

      // If not yet created, do not do anything, just wait
      if (map == null) {
        return;
      }

      updateMapMarkers();

      return () => {
        if (map) {
          map.eachLayer(function (layer: L.Layer) {
            if (layer instanceof L.Marker || layer instanceof L.CircleMarker) {
              map.removeLayer(layer);
            }
          });
        }
      };
    }, [map, pageChangerRef, ships, showOld]);


  const southWest = L.latLng(-90, -180);
  const northEast = L.latLng(90, 180);
  const bounds = L.latLngBounds(southWest, northEast);
  
//   initialMap.on("drag", function () {
//     initialMap.panInsideBounds(bounds, { animate: false });
//   });
//
//   initialMap.on("zoomend", () => {
//     console.log("zoomend");
//     updateMarkersFunc();
//   });
//   initialMap.on("moveend", updateMarkersFunc);
//
//   return initialMap;
    return (
      <MapContainer id={"map-container"} zoom={8} center={[47.0105, 28.8638]} ref={mapRef}
      minZoom={2} maxZoom={17} preferCanvas={true}
      maxBounds={bounds}
      >

        <TileLayer url={"https://tile.openstreetmap.org/{z}/{x}/{y}.png"}
        maxZoom={19}
                   attribution={'&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'}
        />

        {hoverInfo.show && hoverInfo.shipDetails !== null && (
              <div>
                <ShipIconDetails {...hoverInfo}></ShipIconDetails>
              </div>
            )}
      </MapContainer>
      // <div id="map-container">
      //   <div id="map" data-testid="map"></div>
      //   {hoverInfo.show && hoverInfo.shipDetails !== null && (
      //     <div>
      //       <ShipIconDetails {...hoverInfo}></ShipIconDetails>
      //     </div>
      //   )}
      // </div>
    );
  },
);
//
// interface ShipProperties extends GeoJsonProperties {
//   shipId: number;
// }

function addIconsToMap(ships: ShipDetails[], map: L.Map,
                       setHoverInfo: (value: (((prevState: ShipIconDetailsType) => ShipIconDetailsType) | ShipIconDetailsType)) => void,
                       pageChangerRef: React.RefObject<PageChangerRef>) {



  // convert ships to GeoJSON points (format that is required by Supercluster library)
  const points: Feature<Point, {ship: ShipDetails}>[] = ships.map(ship => ({
    type: "Feature",
    properties: { ship: ship },
    geometry: {
      type: "Point",
      coordinates: [ship.lng, ship.lat]
    }
  }));

  // const bounds = map.getBounds();
  // const zoom = map.getZoom();

  // get clusters
  const cluster = new Supercluster({radius: 100, maxZoom: 16});
  cluster.load(points);
//   const {clusters, supercluster}  = useSupercluster(
//     {
//       points,
//       [
//         bounds.getSouthWest().lng,
//       bounds.getSouthWest().lat,
//       bounds.getNorthEast().lng,
//       bounds.getNorthEast().lat
// ],
//      // [bounds.getWest(), bounds.getSouth(), bounds.getEast(), bounds.getNorth()],
//       zoom,
//       options: {},
//       disableRefresh: true
//     }
//   )

  const bounds = map.getBounds();
  const clusters = cluster.getClusters(
    [bounds.getWest(), bounds.getSouth(), bounds.getEast(), bounds.getNorth()],
    map.getZoom()
  )

  clusters.forEach(cluster => {
    const [longitude, latitude] = cluster.geometry.coordinates;
    const { cluster: isCluster, point_count: pointCount } = cluster.properties;

    if (isCluster) {
      L.circleMarker([latitude, longitude]).addTo(map);
      return;
    }

    // not a cluster, just a single ship
    const ship = cluster.properties.ship;

    L.marker([ship.lat, ship.lng], {
      icon: createShipIcon(
        ship.anomalyScore / 100,
        ship.heading,
        ship.speed > 0
      )
    })
      .addTo(map)
      .bindPopup("ID: " + ship.id)
      .on("click", (e) => {
        map.flyTo(e.latlng, Math.max(map.getZoom(), 4));
        handleMouseOutShipIcon(e, setHoverInfo);
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
      .on("mouseout", (e) => {
        handleMouseOutShipIcon(e, setHoverInfo);
      });
  })
}


function oldWayToaddIconsToMap(ships: ShipDetails[], map: L.Map,
                       setHoverInfo: (value: (((prevState: ShipIconDetailsType) => ShipIconDetailsType) | ShipIconDetailsType)) => void,
                       pageChangerRef: React.RefObject<PageChangerRef>) {

  // Add all ship icons to the map
  ships.forEach((ship) => {
    try {
      L.marker([ship.lat, ship.lng], {
        icon: createShipIcon(
          ship.anomalyScore / 100,
          ship.heading,
          ship.speed > 0
        )
      })
        .addTo(map)
        .bindPopup("ID: " + ship.id)
        .on("click", (e) => {
          map.flyTo(e.latlng, Math.max(map.getZoom(), 4));
          handleMouseOutShipIcon(e, setHoverInfo);
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
        .on("mouseout", (e) => {
          handleMouseOutShipIcon(e, setHoverInfo);
        });
    } catch (error) {
      if (error instanceof Error) {
        ErrorNotificationService.addWarning(
          "Error while adding an icon for ship with id " +
          ship.id +
          ": " +
          error.message
        );
      }
    }
  });
}

// Needed for Lint to work (React itself does not require this)
LMap.displayName = "Map";

export default LMap;
export type { MapExportedMethodsType };
