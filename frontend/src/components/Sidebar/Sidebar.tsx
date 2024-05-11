import React from 'react';
import Stack from '@mui/material/Stack';

import '../../styles/common.css';
import '../../styles/sidebar.css';

interface SidebarProps {
    pageChanger: Function
}

/**
 * This prop is the sidebar of the application (third column). It contains three icons that are placeholders
 * for future functionality.
 *
 * @param pageChanger function that, when called, changes the page displayed in the second column.
 */
function Sidebar({ pageChanger } : SidebarProps) {
    // Load the icons
    const shipIcon = require('../../assets/icons/ship.png');
    const bellIcon = require('../../assets/icons/bell-notification.svg').default;
    const settingsIcon = require('../../assets/icons/settings.svg').default;

    // Define the click handlers for the icons
    const onShipIconClicked = () => pageChanger({currentPage: 'anomalyList', shownShipId: ''});
    const onBellIconClicked = () => pageChanger({currentPage: 'notifications', shownShipId: ''});
    const onSettingsIconClicked = () => pageChanger({currentPage: 'settings', shownShipId: ''});

    return (
        <Stack id="sidebar">
            <span className="sidebar-entry" onClick={onShipIconClicked}><img src={shipIcon} className="sidebar-icon"/></span>
            <span className="sidebar-entry" onClick={onBellIconClicked}><img src={bellIcon} className="sidebar-icon" /></span>
            <span className="sidebar-entry" onClick={onSettingsIconClicked}><img src={settingsIcon} className="sidebar-icon" /></span>
        </Stack>
    )
}

export default Sidebar;