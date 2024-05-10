import React from 'react';
import Stack from '@mui/material/Stack';
import Item from '@mui/material/Stack';
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { useState } from 'react';
import Map from './components/Map/Map';
import AnomalyList from './components/AnomalyList/AnomalyList';
import Sidebar from './components/Sidebar/Sidebar';
import ObjectDetails from './components/ObjectDetails/ObjectDetails';

import ShipDetails from './model/ShipDetails';

import './styles/common.css';
import { Browser } from 'leaflet';

// Context that is passed to all children of the app. When a child wants to update
// the second column, it can call the function that is passed in this context.

interface CurrentPage {
    currentPage: string;
    shownShipId: string;
}

function App() {

    // Create state for current page
    const [currentPage, setCurrentPage] = useState({currentPage: 'anomalyList', shownShipId: ''} as CurrentPage);
    console.log("rerendering!" + currentPage.shownShipId)
    const middleColumn = () => {
        switch (currentPage.currentPage) {
            case 'anomalyList':
                return <AnomalyList ships={ships} pageChanger={setCurrentPage} />;
            case 'objectDetails':
                return <ObjectDetails ships={ships} shipId={currentPage.shownShipId} />;
        }
    }


    var ships = [
        new ShipDetails('1', 180, 123.695212883123546, 95.5444375499444,  12, 'The ship has been travelling faster than 30 knots for more than 15 minutes.'),
        new ShipDetails('2', 180, 123.695212883123546, 95.5444375499444,  52, 'The ship has been travelling faster than 30 knots for more than 15 minutes.'),
        new ShipDetails('3', 180, 123.695212883123546, 95.5444375499444,  52, 'The ship has been travelling faster than 30 knots for more than 15 minutes.'),
        new ShipDetails('4', 180, 123.695212883123546, 95.5444375499444,  52, 'The ship has been travelling faster than 30 knots for more than 15 minutes.'),
        new ShipDetails('5', 180, 123.695212883123546, 95.5444375499444,  52, 'The ship has been travelling faster than 30 knots for more than 15 minutes.'),

    ];

    return (
        <div className="App" id="root-div">
            <Stack direction="row">
                <Map />
                {middleColumn()}
                <Sidebar />
            </Stack>
        </div>
    );
}

export default App;