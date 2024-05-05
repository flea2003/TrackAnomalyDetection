import React, {useEffect} from "react";
import L, {bounds} from "leaflet";
import createShipIcon from "./Ship";
import config from "../../config";
import axios from "axios";

/**
 * this is a convenience interface to store the ship details
 */
interface shipDetails{
    name: string,
    color: number,
    heading: number,
    lat: number,
    lng: number
}

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
            const data: shipDetails[] = [
                {name: 'loren iopsum', color: 0.2, heading: 90, lat: -60.53973419889964, lng: 13.606189517209998},
                {name: 'loren iopsum', color: 0.4, heading: 180, lat: 5.695212883123546, lng: 95.5444375499444},
                {name: 'loren iopsum', color: 0.7, heading: 330, lat: -28.31129313464126, lng: 102.9402365060044},
                {name: 'loren iopsum', color: 0.5, heading: 5, lat: -46.33733595441353, lng: -60.29221202458584},
                {name: 'loren iopsum', color: 0.1, heading: 14, lat: 55.501870704302235, lng: -116.84184341971269},
                {name: 'loren iopsum', color: 1, heading: 99, lat: 5.3729452444646455, lng: 175.5605510700476},
                {name: 'loren iopsum', color: 0.3, heading: 180, lat: -44.36008430593716, lng: -26.90496757766911},
                {name: 'loren iopsum', color: 0.6, heading: 180, lat: 87.03237913072971, lng: -152.97588516723232},
                {name: 'loren iopsum', color: 0.6, heading: 50, lat: 86.72079727846875, lng: -99.22131371520763},
                {name: 'loren iopsum', color: 1, heading: 18, lat: -23.69820475750393, lng: 17.42461335534381},
                {name: 'loren iopsum', color: 0.4, heading: 9, lat: -87.6196614306036, lng: 45.808661808603375},
                {name: 'loren iopsum', color: 0, heading: 530, lat: 15.72978708096744, lng: -131.40313285676936},
                {name: 'loren iopsum', color: 0, heading: 44, lat: 88.55234282255597, lng: -112.99297110000691},
                {name: 'loren iopsum', color: 0.1, heading: 25, lat: 44.79167667195594, lng: 106.57851328327268},
                {name: 'loren iopsum', color: 0.2, heading: 12, lat: 10.73649604876438, lng: 145.53449994936426}
            ];
            resolve(data);
            // we shouldn't wait for the data to be fetched, hence I simulate a delay of fetching the ships
        }, 2000); // Simulate a delay of 2 seconds
    });
};



function Map(){

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
                    let dataShips = data as shipDetails[]
                    dataShips.forEach(ship => {
                        // deal with repeated world map : https://stackoverflow.com/questions/33632608/markers-do-not-appear-on-continuous-world-in-leaflet
                        L.marker([ship.lat, ship.lng], {icon: createShipIcon(ship.color, ship.heading)})
                            .addTo(map)
                            .bindPopup(ship.name);
                        L.marker([ship.lat, ship.lng - 360], {icon: createShipIcon(ship.color, ship.heading)})
                            .addTo(map)
                            .bindPopup(ship.name);
                        L.marker([ship.lat, ship.lng + 360], {icon: createShipIcon(ship.color, ship.heading)})
                            .addTo(map)
                            .bindPopup(ship.name);
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

    return <div id = 'map'></div>
}

export default Map;