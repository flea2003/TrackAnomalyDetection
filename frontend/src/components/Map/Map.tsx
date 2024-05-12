import React, {useEffect, useState} from "react";
import L, {bounds} from "leaflet";
import createShipIcon from "../ShipIcon/ShipIcon";
import config from "../../../config";
import axios from "axios";
import '../../styles/map.css'
import '../../styles/common.css'

import ShipDetails from "../../model/ShipDetails";
import assert from "assert";

interface MapProps {
    ships: ShipDetails[],
    pageChanger: Function
}

/**
 * This function creates a Leaflet map with the initial settings. It is called only once, when the component is mounted.
 * @returns the created map
 */
function getInitialMap() {
    const initialMap = L.map('map', {
        minZoom: 2,
        maxZoom: 17,
    }).setView([47.0105, 28.8638], 8);

    let southWest = L.latLng(-90, -180);
    let northEast = L.latLng(90, 180);
    let bounds = L.latLngBounds(southWest, northEast);

    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(initialMap);

    initialMap.setMaxBounds(bounds);
    initialMap.on('drag', function() {
        initialMap.panInsideBounds(bounds, { animate: false });
    });

    return initialMap
}

/**
 * This component is the first column of the main view of the application. It displays the map with all the ships.
 * A list of ships is passed as a prop.
 *
 * @param ships the ships to display on the map
 * @param pageChanger function that, when called, changes the page displayed in the second column.
 */
function Map({ ships, pageChanger } : MapProps){

    // Initialize the map as state, since we want to have a single instance
    const [map, setMap] = useState<L.Map | null>(null);

    // Everthing to do with the map updates should be done inside of useEffect
    useEffect(() => {

        // If the map is null, we need to create it. We do it once, with state
        if (map == null) {
            const initialMap = getInitialMap();
            setMap(initialMap);
        }

        // If not yet created, do not do anything, just wait
        if(map == null) {
            return;
        }

        // Add all ship icons to the map
        ships.forEach(ship => {
            L.marker([ship.lat, ship.lng], {icon: createShipIcon(ship.anomalyScore / 100, ship.heading)})
                .addTo(map)
                .bindPopup(ship.id)
                .on('click', () => {
                    pageChanger({currentPage: 'objectDetails', shownShipId: ship.id});
                });
        });

        return () => {

            if (map) {
                map.eachLayer(function (layer : L.Layer) {
                    if (layer instanceof L.Marker) {
                        map.removeLayer(layer);
                    }
                });
            }
        };

    }, [ships]);

    return (
        <div id="map-container">
            <div id="map" data-testid="map"></div>
        </div>
    );
}


export default Map;
