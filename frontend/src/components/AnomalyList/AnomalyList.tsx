import React from 'react';
import Stack from '@mui/material/Stack';
import AnomalyListEntry from './AnomalyListEntry';
import List from '@mui/material/List';

import ShipDetails from '../../model/ShipDetails';

import '../../styles/common.css';
import '../../styles/anomalyList.css';

interface AnomalyListProps {
    ships: ShipDetails[],
    pageChanger: Function
}


/**
 * This component is the second column of the main view of the application. It essentially displays
 * the "Anomaly List" title and renders all the AnomalyListEntry components.
 *
 * @param ships a list of ships to display in the list
 * @param pageChanger function that, when called, changes the page displayed in the second column.
 */
function AnomalyList({ ships, pageChanger }: AnomalyListProps) {

    const listEntries = [];
    for (var i = 0; i < ships.length; i++) {
        listEntries.push(<AnomalyListEntry key={i} shipDetails={ships[i]} pageChanger={pageChanger} />);
    }

    return (
        <Stack id="anomaly-list-container">
            <span id="anomaly-list-title">Anomaly list</span>
            <List id="anomaly-list-internal-container" style={{maxHeight: '100%', overflow: 'auto', padding: '0'}}>
                {listEntries}
            </List>
        </Stack>
    )
}

export default AnomalyList;
