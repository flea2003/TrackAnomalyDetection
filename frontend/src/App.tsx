import React from 'react';
import Map from './components/map/Map';
import AnomalyList from './components/AnomalyList/AnomalyList';
import Sidebar from './components/sidebar/Sidebar';
import Stack from '@mui/material/Stack';
import Item from '@mui/material/Stack';
import './styles/common.css';

function App() {
    return (
        <div className="App" id="root-div">
            <Stack direction="row">
                <Map />
                <AnomalyList />
                <Sidebar />
            </Stack>
        </div>
    );
}

export default App;