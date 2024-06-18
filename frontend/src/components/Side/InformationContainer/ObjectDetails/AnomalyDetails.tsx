import React from "react";
import ShipDetails from "../../../../model/ShipDetails";
import TimeUtilities from "../../../../utils/TimeUtilities";
import { Stack } from "react-bootstrap";

import "../../../../styles/object-details/anomalyDetails.css";
import "../../../../styles/common.css";
import "../../../../styles/object-details/objectDetailsEntry.css";

interface ObjectDetailsProps {
  ship: ShipDetails;
  addAnomalyScore: boolean;
}

/**
 * Component that displays the anomaly information of a ship
 *
 * @param ship ships whose anomaly information is being displayed
 * @param addAnomalyScore boolean that defines whether anomaly score should be displayed
 * @constructor
 */
function AnomalyDetails({ ship, addAnomalyScore }: ObjectDetailsProps) {
  return (
    <div className="anomaly-details-container">
      {addAnomalyScore && (
        <Stack className="anomaly-score-div">
          <div className="anomaly-details-subtitle">Anomaly Score</div>
          <Stack className="anomaly-details-bulletlist">
            {getAnomalyScore(ship)}
          </Stack>
        </Stack>
      )}

      <div className="anomaly-details-subtitle">Anomaly Description</div>
      <Stack className="anomaly-details-bulletlist">
        {getExplanationList(ship.explanation)}
      </Stack>
      <div className="anomaly-details-subtitle">
        Maximum Anomaly Information
      </div>
      <Stack className="anomaly-details-bulletlist">
        {getMaximumAnomalyInfoList(ship)}
      </Stack>
    </div>
  );
}

/**
 * Function that returns a bullet-list of all reasons why a ship is anomalous
 *
 * @param str string that contains the description of all anomaly explanations
 */
function getExplanationList(str: string) {
  str = str.trim();

  if (str === "") {
    return (
      <ul className="anomaly-details-entry-value">
        <li key={0}>No anomalous behaviour registered.</li>
      </ul>
    );
  }

  return (
    <ul className="anomaly-details-entry-value">
      {str.split("\n").map((line, index) => (
        <li key={index}>{line}</li>
      ))}
    </ul>
  );
}

/**
 * Function that returns information about the maximum anomaly
 *
 * @param ship ship whose data is being considered
 */
function getMaximumAnomalyInfoList(ship: ShipDetails) {
  if (ship.maxAnomalyScore === undefined) {
    return (
      <ul className="anomaly-details-entry-value">
        <li key={0}>No anomalous behaviour registered.</li>
      </ul>
    );
  }

  return (
    <ul className="anomaly-details-entry-value">
      <li key={0}>Score: {ship.maxAnomalyScore}%</li>
      <li key={1}>
        Obtained: {TimeUtilities.reformatTimestamp(ship.correspondingTimestamp)}
      </li>
    </ul>
  );
}

/**
 * Function that returns a UI component describing the anomaly score
 *
 * @param ship ship whose data is being considered
 */
function getAnomalyScore(ship: ShipDetails) {
  if (ship.anomalyScore === undefined || ship.anomalyScore === -1) {
    return (
      <ul className="anomaly-details-entry-value">
        <li key={0}>No anomalous behaviour registered.</li>
      </ul>
    );
  }

  return (
    <ul className="anomaly-details-entry-value">
      <li key={0}>{ship.anomalyScore}%</li>
    </ul>
  );
}

export default AnomalyDetails;
