import React from 'react';
import Stack from '@mui/material/Stack';
import PropTypes from 'prop-types';

import ShipDetails from '../../model/ShipDetails';
import { calculateAnomalyColor } from '../../utils/AnomalyColorCalculator';

import '../../styles/common.css';
import '../../styles/anomalyListEntry.css';
import { randomInt } from 'crypto';

interface AnomalyListEntryProps {
    shipDetails: ShipDetails,
    pageChanger: Function
}

/**
 * This component is a single entry in the Anomaly List. It displays the anomaly score and an icon of an object.
 * The object to render is passed as a prop.
 *
 * @param props properties passed to this component. Most importantly, it contains the ship details to display.
 */
function AnomalyListEntry({ shipDetails, pageChanger } : AnomalyListEntryProps) {

    const shipIcon = require('../../assets/icons/ship.png');

    const shipAnomalyScore = shipDetails.anomalyScore;
    const color = calculateAnomalyColor(shipAnomalyScore);

    const onClick = () => {
        pageChanger({currentPage: 'objectDetails', shownShipId: shipDetails.id});
    }

    return (
        <Stack direction="row" className="anomaly-list-entry" spacing={0} style={{backgroundColor: color}} onClick={onClick}>
            <span className="anomaly-list-entry-icon-container"><img src={shipIcon} className="anomaly-list-entry-icon"/></span>
            <span className="anomaly-list-entry-main-text">Anomaly score: <span className="anomaly-list-entry-score">{shipAnomalyScore}</span> </span>
            <span></span> {/* Empty span for spacing */}
        </Stack>
    )
}

export default AnomalyListEntry;