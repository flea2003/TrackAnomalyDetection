import React from 'react';
import Stack from '@mui/material/Stack';
import AnomalyListEntry from './AnomalyListEntry';
import List from '@mui/material/List';

import '../../styles/common.css';
import '../../styles/anomalyList.css';

/**
 * This component is the second column of the main view of the application. It essentially displays
 * the "Anomaly List" title and renders all the AnomalyListEntry components.
 */
function AnomalyList() {

    const entries = [];
    const entryCount = 40;
    for (var i = 0; i < entryCount; i++) {
        entries.push(<AnomalyListEntry />);
    }

    return (
        <Stack id="anomaly-list-container">
            <span id="anomaly-list-title">Anomaly List</span>
            <List style={{maxHeight: '100%', overflow: 'auto', padding: '0'}}>
                {entries}
            </List>
        </Stack>
    )
}

export default AnomalyList;
