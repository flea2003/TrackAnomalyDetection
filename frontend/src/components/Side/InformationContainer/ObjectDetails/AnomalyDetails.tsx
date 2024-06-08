import React from "react";

import "../../../../styles/common.css";
import "../../../../styles/ship-details/shipDetailsEntry.css";
import ShipDetails from "../../../../model/ShipDetails";
import ShipNotification from "../../../../model/ShipNotification";
import { CurrentPage } from "../../../../App";

import "../../../../styles/ship-details/anomalyDetails.css";
import TimeUtilities from "../../../../utils/TimeUtilities";
import { Stack } from "react-bootstrap";


interface ObjectDetailsProps {
  ship: ShipDetails;
  addAnomalyScore: boolean;
}


function AnomalyDetails({ship, addAnomalyScore} : ObjectDetailsProps) {
  return (
    <div className="anomaly-details-container">
      {
        addAnomalyScore &&
        (
          <Stack className="anomaly-score-div">
            <div className="anomaly-details-subtitle">Anomaly Score</div>
            <Stack className="anomaly-details-bulletlist">
              {getAnomalyScore(ship)}
            </Stack>
          </Stack>
        )
      }

      <div className="anomaly-details-subtitle">Anomaly Description</div>
      <Stack className="anomaly-details-bulletlist">
        {getExplanationList(ship.explanation)}
      </Stack>
      <div className="anomaly-details-subtitle">Maximum Anomaly Information</div>
      <Stack className="anomaly-details-bulletlist">
        {getMaximumAnomalyInfoList(ship)}
      </Stack>
    </div>
  );
}

function getExplanationList(str: string) {
  str = str.trim();

  if (str === "") {
    return <ul className="anomaly-details-entry-value">
      <li key={0}>No anomalous behaviour registered</li>
    </ul>;
  }

  return (
    <ul className="anomaly-details-entry-value">
      {str.split("\n").map((line, index) => (
        <li key={index}>{line}</li>
      ))}
    </ul>
  );
}

function getMaximumAnomalyInfoList(ship: ShipDetails) {
  if (ship.explanation === "") {
    return <ul className="anomaly-details-entry-value">
      <li key={0}>No anomalous behaviour registered</li>
    </ul>;
  }

  return (
    <ul className="anomaly-details-entry-value">
      <li key={0}>Score: {ship.anomalyScore}%</li>
      <li key={1}>Obtained: {TimeUtilities.reformatTimestamp(ship.correspondingTimestamp)}</li>
    </ul>
  );
}

function getAnomalyScore(ship: ShipDetails) {
  if (ship.anomalyScore === undefined || ship.anomalyScore===-1) {
    return <ul className="anomaly-details-entry-value">
      <li key={0}>No anomalous behaviour registered</li>
    </ul>;
  }

  return (
    <ul className="anomaly-details-entry-value">
      <li key={0}>{ship.anomalyScore}%</li>
    </ul>
  );
}

export default AnomalyDetails;
