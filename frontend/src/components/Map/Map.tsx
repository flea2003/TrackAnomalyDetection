import React, {useEffect, useState} from "react";
import L, {bounds} from "leaflet";
import createShipIcon from "../ship/Ship";
import config from "../../../config";
import axios from "axios";
import '../../styles/map.css'
import '../../styles/common.css'

import ShipDetails from "../../model/ShipDetails";


/**
 * This is how our API call to backend would look like
 */
// const fetchInitial = () => {
//     axios.get(config.apiUrl)
//         .then(resp => {
//             const { data } = resp
//             if (data) {
//                 return data
//             }else{
//                 throw new Error("Couldn't fetch the ships ... sorry");
//             }
//         })
// }

/**
 * this function simulates our fetch for now. It is an asynchronous function which takes 2 seconds to execute
 * It will then call resolve data which will populate the map with the ships.
 */
const fetchInitial = () => {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            const data: ShipDetails[] = [
                new ShipDetails('loren iopsum', 180, 123.695212883123546, 95.5444375499444,  12, 'The ship has been travelling faster than 30 knots for more than 15 minutes.'),
                new ShipDetails('loren iopsum', 180, 50.695212883123546, 95.5444375499444,  62, 'The ship has been travelling faster than 30 knots for more than 15 minutes.'),
            ];
            resolve(data);
            // we shouldn't wait for the data to be fetched, hence I simulate a delay of fetching the ships
        }, 2000); // Simulate a delay of 2 seconds
    });
};



function Map(){

    const [selectedShip, setSelectedShip] = useState<ShipDetails | null>(null);


    useEffect(() => {

        let southWest = L.latLng(-90, -180);
        let northEast = L.latLng(90, 180);
        let bounds = L.latLngBounds(southWest, northEast);

        const map: L.Map = L.map('map', {
            minZoom: 2,
            maxZoom: 17,
        }).setView([47.0105, 28.8638], 8);

        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(map);

        map.setMaxBounds(bounds);
        map.on('drag', function() {
            map.panInsideBounds(bounds, { animate: false });
        });


        /**
         * When the map is ready, and resolve is called, the map will be populated with ships
         * For now this is a very dumb lazy polling
         */
        map.whenReady(() => {
            const dumbPolling = () => {
                fetchInitial().then((data) => {
                    // you can look at the console and see that the code is indeed executed
                    console.log('rendering')
                    let dataShips = data as ShipDetails[]
                    dataShips.forEach(ship => {
                        // deal with repeated world map : https://stackoverflow.com/questions/33632608/markers-do-not-appear-on-continuous-world-in-leaflet
                        L.marker([ship.lat, ship.lng], {icon: createShipIcon(ship.anomalyScore / 100, ship.heading)})
                            .addTo(map)
                            .bindPopup(ship.name)
                            .on('click', () => {
                                setSelectedShip(ship);
                            });
                        L.marker([ship.lat, ship.lng - 360], {icon: createShipIcon(ship.anomalyScore / 100, ship.heading)})
                            .addTo(map)
                            .bindPopup(ship.name)
                            .on('click', () => {
                                setSelectedShip(ship);
                            });
                        L.marker([ship.lat, ship.lng + 360], {icon: createShipIcon(ship.anomalyScore / 100, ship.heading)})
                            .addTo(map)
                            .bindPopup(ship.name)
                            .on('click', () => {
                                setSelectedShip(ship);
                            });
                    });
                    return;
                }).then(() => { // thanks to ChatGPT for helping me set up this wonderful callback
                    setTimeout(dumbPolling, 1000);
                });
            }

            dumbPolling();
        })


        return () => {
            map.remove();
        };

    }, []);

    function closeInfo():void{
        setSelectedShip(null);
    }

    return (
        <div id="map-container">
            <div id="map"></div>
        </div>
    );
}


export default Map;