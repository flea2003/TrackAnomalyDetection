import ShipDetails from "../../../../model/ShipDetails";
import ShipNotification from "../../../../model/ShipNotification";
import { CurrentPage } from "../../../../App";
import React, { useEffect, useState } from "react";
import HttpSender from "../../../../utils/communication/HttpSender";
import plottingConfig from "../../../../configs/plottingConfig.json";
import Plot from "react-plotly.js";
import anomalyScorePlotStyle from "../../../../configs/anomalyScorePlotStyle.json";

interface ScorePlotProps {
  ship: ShipDetails;
  notifications: ShipNotification[];
  pageChanger: (currentPage: CurrentPage) => void;
}

function ScorePlot(props: ScorePlotProps){

  // Extract the props
  const selectedShipId = props.ship.id;

  const threshold = plottingConfig.notificationThreshold;

  const allNotifications = props.notifications;

  const shipNotifications = allNotifications.filter((notification) =>
    notification.shipDetails.id === selectedShipId
  );

  const notificationScores = shipNotifications.map((notification) => notification.shipDetails.anomalyScore);
  const notificationTimestamps = shipNotifications.map((notification) => notification.shipDetails.timestamp);

  const [ shipHistory, setShipHistory ] = useState<ShipDetails[]>([]);

  // Requires filtering out the default values assigned to incomplete details
  const scoreHistory = shipHistory.map((ship) => ship.anomalyScore);
  const timestampHistory = shipHistory.map((ship) => ship.timestamp);

  // Plot datapoint descriptions
  const anomalyScoreDescriptions = scoreHistory.map((score, index) => {
    return `Score: ${score}<br>Timestamp: ${timestampHistory[index]}`
  });

  const notificationDescriptions = notificationScores.map((score, index) => {
    return `Score: ${score}<br>Timestamp: ${notificationTimestamps[index]}`
  });

  return (
    <Plot
      data={[
        {
          x: timestampHistory,
          y: scoreHistory,
          type: 'scatter',
          mode: 'lines',
          name: 'Anomaly Scores',
          text: anomalyScoreDescriptions,
          hoverinfo: 'text',
          line: { color: 'blue'},
        },
        {
          x: notificationTimestamps,
          y: notificationScores,
          type: 'scatter',
          mode: 'markers',
          name: 'Notifications',
          text: notificationDescriptions,
          hoverinfo: 'text',
          marker: { color: 'yellow', size: 8}
        }
      ]}
      layout={anomalyScorePlotStyle}
    />
  );
}

export default ScorePlot;