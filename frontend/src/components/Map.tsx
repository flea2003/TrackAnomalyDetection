import React, {useEffect} from "react";
import L from "leaflet";

import './Map.css'

function Map(){

    useEffect(() => {
        const map: L.Map = L.map('map').setView([47.0105, 28.8638], 8);

        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(map);

        return () => {
            map.remove();
        };

    }, []);

    return <div id = 'map'></div>
}

export default Map;