import ShipDetails from "../../../../model/ShipDetails";
import ShipNotification from "../../../../model/ShipNotification";
import { CurrentPage } from "../../../../App";
import Stack from "@mui/material/Stack";
import React from "react";
import HttpSender from "../../../../utils/communication/HttpSender";
import plottingConfig from "../../../../configs/plottingConfig.json";
import Plot from "react-plotly.js";

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

  const selectedShip = allShips.find((x) => x.id === props.shipId);

  if (selectedShip === undefined) {
    return shipNotFoundElement();
  }

  const shipNotifications = allNotifications.filter((notification) =>
    notification.shipDetails.id === props.shipId
  );

  const notificationScores = allNotifications.map((notification) => notification.shipDetails.getAnomalyScore());
  const notificationTimestamps = allNotifications.map((notification) => notification.shipDetails.timestamp);

  // Fetch the ShipDetails history of the selected ship

  const shipHistory: Promise<ShipDetails[]> = HttpSender.get(plottingConfig.historyEndpoint + props.shipId);

  const scoreHistory = shipHistory.then((history) => history.map((ship) => ship.getAnomalyScore()));
  const timestampHistory = shipHistory.then((history) => history.map((ship) => ship.timestamp));


  return (
    <Plot
      data={[
        {
          x: timestampHistory,
          y: scoreHistory,
          type: 'line',
          mode: 'lines+markers',
          marker: { color: 'red' },
        },
        { type: 'bar', x: notificationTimestamps, y: notificationScores },
      ]}
      layout={{ width: 720, height: 440, title: 'Anomaly Score Plot' }}
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