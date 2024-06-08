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
    // const [map, setMap] = useState<L.Map | null>(null);
    const mapRef = useRef(null);

    console.log("cia kazka print -Augustinas 2024")

    // TODO add below functions
    // // Define the methods that will be reachable from the parent
    // useImperativeHandle(ref, () => ({
    //   centerMapOntoShip(ship: ShipDetails) {
    //     if (map == null) {
    //       ErrorNotificationService.addWarning(
    //         "map is null in the call centerMapOntoShip",
    //       );
    //       return;
    //     }
    //
    //     // Check if requested ship still exists
    //     if (ship === undefined) return;
    //     if (ships.find((x) => x.id === ship.id) === undefined) return;
    //
    //     map.flyTo(
    //       [ship.lat, ship.lng],
    //       mapStyleConfig["zoom-level-when-clicked-on-ship-in-list"],
    //       {
    //         animate: true,
    //         duration: mapStyleConfig["transition-time"],
    //       },
    //     );
    //   },
    // }));

    // Initialize the hoverInfo variable that will manage the display of the
    // pop-up div containing reduced information about a particular ship
    const [hoverInfo, setHoverInfo] = useState<ShipIconDetailsType>({
      show: false,
      x: 0,
      y: 0,
      shipDetails: null,
    } as ShipIconDetailsType);

    useEffect(() => {
      const map = mapRef.current as L.Map | null;

      if (map != null) {
        map.on("drag", () => {
          map.panInsideBounds(getMapGlobalBounds(), {animate: false});
        })

        const cluster = loadPointsToSupercluster(ships);

        const updateMapMarkers = () => {
          console.log("update map markers called");
          clearMarkersFromMap(map);
          addIconsToMap(cluster, map, setHoverInfo, pageChangerRef);
        }

        updateMapMarkers();

        map.on("moveend", updateMapMarkers);

        return () => {
          clearMarkersFromMap(map);

          if (map != null) {
            map.off("moveend", updateMapMarkers);
          }
        }
      }

      return () => clearMarkersFromMap(map);
    }, [pageChangerRef, ships]);

    return (
      <MapContainer id={"map-container"} zoom={8} center={[47.0105, 28.8638]} ref={mapRef}
      minZoom={2} maxZoom={17} preferCanvas={true}
      maxBounds={getMapGlobalBounds()}
      >

        <TileLayer url={"https://tile.openstreetmap.org/{z}/{x}/{y}.png"}
        maxZoom={19}
                   attribution={'&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'}
        />

        {hoverInfo.show && hoverInfo.shipDetails !== null
          && (
              <div>
                <ShipIconDetails {...hoverInfo}></ShipIconDetails>
              </div>
            )}
      </MapContainer>
    );
  },
);

// TODO make this async
function addIconsToMap(cluster: Supercluster, map: L.Map,
                       setHoverInfo: (value: (((prevState: ShipIconDetailsType) => ShipIconDetailsType) | ShipIconDetailsType)) => void,
                       pageChangerRef: React.RefObject<PageChangerRef>) {

  const bounds = map.getBounds();
  const clusters = cluster.getClusters(
    [bounds.getWest(), bounds.getSouth(), bounds.getEast(), bounds.getNorth()],
    map.getZoom()
  )

  for (const cluster1 of clusters) {
    addMarker(cluster1, map, setHoverInfo, pageChangerRef);

  }
}

function addMarker(cluster1:  Supercluster.ClusterFeature<Supercluster.AnyProps> | Supercluster.PointFeature<Supercluster.AnyProps>,
                         map: L.Map,
                         setHoverInfo: (value: (((prevState: ShipIconDetailsType) => ShipIconDetailsType) | ShipIconDetailsType)) => void,
                         pageChangerRef: React.RefObject<PageChangerRef>) {

  // const [longitude, latitude] = cluster1.geometry.coordinates;
  // const { cluster: isCluster, point_count: pointCount } = cluster1.properties;
  //
  // if (isCluster) {
  //   L.circleMarker([latitude, longitude]).addTo(map);
  //   return;
  // }
  //
  // // not a cluster, just a single ship
  // const ship = cluster1.properties.ship;
  //
  // L.marker([ship.lat, ship.lng], {
  //   icon: createShipIcon(
  //     ship.anomalyScore / 100,
  //     ship.heading,
  //     ship.speed > 0
  //   )
  // })
  //   .addTo(map)
  //   .bindPopup("ID: " + ship.id)
  //   .on("click", (e) => {
  //     map.flyTo(e.latlng, Math.max(map.getZoom(), 4));
  //     handleMouseOutShipIcon(e, setHoverInfo);
  //     if (pageChangerRef.current !== null) {
  //       pageChangerRef.current.pageChanger({
  //         currentPage: "objectDetails",
  //         shownItemId: ship.id
  //       });
  //     }
  //   })
  //   .on("mouseover", (e) => {
  //     handleMouseOverShipIcon(e, ship, map, setHoverInfo);
  //   })
  //   .on("mouseout", (e) => {
  //     handleMouseOutShipIcon(e, setHoverInfo);
  //   });
}
//
// function loadPointsToSupercluster(ships: ShipDetails[]) {
//   // convert ships to GeoJSON points (format that is required by Supercluster library)
//   const points: Feature<Point, {ship: ShipDetails}>[] = ships.map(ship => ({
//     type: "Feature",
//     properties: { ship: ship },
//     geometry: {
//       type: "Point",
//       coordinates: [ship.lng, ship.lat]
//     }
//   }));
//
//   // const bounds = map.getBounds();
//   // const zoom = map.getZoom();
//
//   // get clusters
//   const cluster = new Supercluster({radius: 80});
//   cluster.load(points);
//
//   return cluster;
// }

function getMapGlobalBounds() {
  const southWest = L.latLng(-90, -180);
  const northEast = L.latLng(90, 180);
  return L.latLngBounds(southWest, northEast);
}

function clearMarkersFromMap(map: L.Map | null) {
  if (map == null) {
    return;
  }

  map.eachLayer(function (layer: L.Layer) {
    if (layer instanceof L.Marker || layer instanceof L.CircleMarker) {
      map.removeLayer(layer);
    }
  });
}

// Needed for Lint to work (React itself does not require this)
LMap.displayName = "Map";

export default LMap;
export type { MapExportedMethodsType };
