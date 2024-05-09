import React from 'react';
import Stack from '@mui/material/Stack';
import PropTypes from 'prop-types';

import '../../styles/common.css';
import '../../styles/anomalyListEntry.css';
import { randomInt } from 'crypto';

interface AnomalyListEntryProps {
    someInformation: boolean
}

/**
 * Given an anomaly score, this function calculates the color of the anomaly list entry.
 * 
 * @param shipAnomalyScore the anomaly score of the ship
 * @returns the color of the anomaly list entry
 */
function calculateAnomalyColor(shipAnomalyScore : number) {
    shipAnomalyScore /= 100.0;
    const red = Math.floor(255 * (1 - shipAnomalyScore));
    const green = Math.floor(255 * shipAnomalyScore);
    const alpha = 0.4;
    return `rgba(${red}, ${green}, 0, ${alpha})`;
}

/**
 * This component is a single entry in the Anomaly List. It displays the anomaly score and an icon of an object.
 * The object to render is passed as a prop.
 */
function AnomalyListEntry({ someInformation } : AnomalyListEntryProps) {

    const shipIcon = require('../../assets/icons/ship.png');

    const shipAnomalyScore = Math.floor((Math.random()) * 100); // TODO: To be deduced from the ship object passed as a prop
    const color = calculateAnomalyColor(shipAnomalyScore);

    return (
        <Stack direction="row" className="anomaly-list-entry" spacing={0} style={{backgroundColor: color}} >
            <span className="anomaly-list-entry-icon-container"><img src={shipIcon} className="anomaly-list-entry-icon"/></span>
            <span className="anomaly-list-entry-main-text">Anomaly score: <span className="anomaly-list-entry-score">{shipAnomalyScore}</span> </span>
            <span></span> {/* Empty span for spacing */}
        </Stack>
    )
}

 AnomalyListEntry.defaultProps = {
    someInformation: false
 };

export default AnomalyListEntry;