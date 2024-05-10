import React from 'react';
import Stack from '@mui/material/Stack';
import Item from '@mui/material/Stack';
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { useState, useEffect } from 'react';
import Map from './components/Map/Map';
import AnomalyList from './components/AnomalyList/AnomalyList';
import Sidebar from './components/Sidebar/Sidebar';
import ObjectDetails from './components/ObjectDetails/ObjectDetails';
import ShipDetails from './model/ShipDetails';
import ShipService from './services/ShipService';

import './styles/common.css';
import { Browser } from 'leaflet';

/**
 * Interface for storing the type of component that is currently displayed in the second column.
 */
interface CurrentPage {
    currentPage: string;
    shownShipId: string;
}

function App() {

    // Create state for current page
    const [currentPage, setCurrentPage] = useState({currentPage: 'anomalyList', shownShipId: ''} as CurrentPage);
    const middleColumn = () => {
        switch (currentPage.currentPage) {
            case 'anomalyList':
                return <AnomalyList ships={ships} pageChanger={setCurrentPage} />;
            case 'objectDetails':
                return <ObjectDetails ships={ships} shipId={currentPage.shownShipId} pageChanger={setCurrentPage}/>;
        }
    }

    // Put the ships as state
    const [ships, setShips] = useState<ShipDetails[]>([]);

    // Every 1s update the anomaly score of all ships by querying the server
    useEffect( () => {
        setInterval(() => {
            // Query for ships. When the results arrive, update the state
            ShipService.queryBackendForShipsArray().then(
                (shipsArray : ShipDetails[]) => {
                    setShips(shipsArray);
                }
            );

        }, 1000);
    }, []);

    // Return the main view of the application
    return (
        <div className="App" id="root-div">
            <Stack direction="row">
                <Map ships={ships} pageChanger={setCurrentPage} />
                {middleColumn()}
                <Sidebar />
            </Stack>
        </div>
    );
}

export default App;