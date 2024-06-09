import ShipDetails from "../../../../model/ShipDetails";
import ShipNotification from "../../../../model/ShipNotification";
import { CurrentPage } from "../../../../App";
import Stack from "@mui/material/Stack";
import React, { useEffect, useState } from "react";
import HttpSender from "../../../../utils/communication/HttpSender";
import plottingConfig from "../../../../configs/plottingConfig.json";
import Plot from "react-plotly.js";
import anomalyScorePlotStyle from "../../../../configs/anomalyScorePlotStyle.json";

interface ScorePlotProps {
  ships: ShipDetails[];
  notifications: ShipNotification[];
  shipId: number;
  pageChanger: (currentPage: CurrentPage) => void;
}

function ScorePlot(props: ScorePlotProps){

  // Extract the props
  const allShips = props.ships;

  const allNotifications = props.notifications;

  const selectedShip = allShips.find((ship) => ship.id === props.shipId);

  const [ shipHistory, setShipHistory ] = useState<ShipDetails[]>([]);

  useEffect(() => {
    const fetchShipHistory = async () => {
      const fetchedShipHistory = await HttpSender.get(plottingConfig.historyEndpoint + props.shipId);
      if (fetchedShipHistory) {
        setShipHistory(fetchedShipHistory);
      }
    };
    fetchShipHistory();
  }, [props.shipId]);

  if (selectedShip === undefined) {
    return shipNotFoundElement();
  }

  // Requires filtering out the default values assigned to incomplete details
  const scoreHistory = shipHistory.map((ship) => ship.getAnomalyScore());
  const timestampHistory = shipHistory.map((ship) => ship.timestamp);

  const shipNotifications = allNotifications.filter((notification) =>
    notification.shipDetails.id === props.shipId
  );

  const notificationScores = shipNotifications.map((notification) => notification.shipDetails.getAnomalyScore());
  const notificationTimestamps = shipNotifications.map((notification) => notification.shipDetails.timestamp);

  // Plot descriptions
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

function shipNotFoundElement() {
  return (
    <Stack id="object-details-container">
      <span className="object-details-title">
        Object ID:&nbsp;&nbsp;
        <span className="object-details-title-id">Not found</span>
      </span>
    </Stack>
  );
}

export default ScorePlot;