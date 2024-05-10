import React from 'react';
import Stack from '@mui/material/Stack';
import AnomalyListEntry from './AnomalyListEntry';
import List from '@mui/material/List';

import ShipDetails from '../../model/ShipDetails';

import '../../styles/common.css';
import '../../styles/anomalyList.css';

/**
 * This component is the second column of the main view of the application. It essentially displays
 * the "Anomaly List" title and renders all the AnomalyListEntry components.
 *
 * @param props properties passed to this component. Most importantly, it contains the list of ships to display.
 */
function AnomalyList(props: { ships: ShipDetails[] }) {

    const ships = props.ships;

    const listEntries = [];
    for (var i = 0; i < ships.length; i++) {
        listEntries.push(<AnomalyListEntry shipDetails={ships[i]} />);
    }

    return (
        <Stack id="anomaly-list-container">
            <span id="anomaly-list-title">Anomaly list</span>
            <List id="anomaly-list-internal-container"style={{maxHeight: '100%', overflow: 'auto', padding: '0'}}>
                {listEntries}
            </List>
        </Stack>
    )
}

export default AnomalyList;
