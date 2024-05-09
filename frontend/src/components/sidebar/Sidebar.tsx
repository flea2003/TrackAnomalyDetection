import React from 'react';
import Stack from '@mui/material/Stack';

import '../../styles/common.css';
import '../../styles/sidebar.css';

function Sidebar() {
    const shipIcon = require('../../assets/icons/ship.png');
    const bellIcon = require('../../assets/icons/bell-notification.svg').default;
    const settingsIcon = require('../../assets/icons/settings.svg').default;

    return (
        <Stack id="sidebar">
            <span className="sidebar-entry"><img src={shipIcon} className="sidebar-icon"/></span>
            <span className="sidebar-entry"><img src={bellIcon} className="sidebar-icon" /></span>
            <span className="sidebar-entry"><img src={settingsIcon} className="sidebar-icon" /></span>
        </Stack>
    )
}

export default Sidebar;