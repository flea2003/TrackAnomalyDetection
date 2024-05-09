import React from 'react';
import Stack from '@mui/material/Stack';
import PropTypes from 'prop-types';

import '../../styles/common.css';
import '../../styles/anomalyListEntry.css';

interface AnomalyListEntryProps {
    someInformation: boolean
}

function AnomalyListEntry({ someInformation } : AnomalyListEntryProps) {
    const shipIcon = require('../../assets/icons/ship.png');

    return (
        <Stack direction="row" className="anomaly-list-entry" spacing={0} >
            <span className="anomaly-list-entry-icon-container"><img src={shipIcon} className="anomaly-list-entry-icon"/></span>
            <span className="anomaly-list-entry-main-text">Anomaly score: <span className="anomaly-list-entry-score">69%</span> </span>
            <span></span> {/* Empty span for spacing */}
        </Stack>
    )
}

 AnomalyListEntry.defaultProps = {
    someInformation: false
 };

export default AnomalyListEntry;