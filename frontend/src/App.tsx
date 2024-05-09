import React from 'react';
import Stack from '@mui/material/Stack';
import Item from '@mui/material/Stack';
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Map from './components/Map/Map';
import AnomalyList from './components/AnomalyList/AnomalyList';
import Sidebar from './components/Sidebar/Sidebar';
import ObjectDetails from './components/ObjectDetails/ObjectDetails';

import './styles/common.css';
import { Browser } from 'leaflet';

// Context that is passed to all children of the app. When a child wants to update
// the second column, it can call the function that is passed in this context.


function App() {
    var fakeShip = { name: "Fake Ship", color: 0, heading: 32.1, lat: 12.121, lng: 54.123};
    return (
        <div className="App" id="root-div">
            <Stack direction="row">
                <Map />
                <BrowserRouter>
                    <Routes>
                        <Route path="/" element={<AnomalyList />} />
                        <Route path="/objectDetails" element={<ObjectDetails ship={fakeShip} />} />
                    </Routes>
                </BrowserRouter>
                <Sidebar />
            </Stack>
        </div>
    );
}

export default App;