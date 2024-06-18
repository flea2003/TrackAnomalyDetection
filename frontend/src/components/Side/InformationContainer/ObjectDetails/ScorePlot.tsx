import ShipDetails from "../../../../model/ShipDetails";
import plottingConfig from "../../../../configs/plottingConfig.json";
import Plot from "react-plotly.js";
import anomalyScorePlotStyle from "../../../../configs/anomalyScorePlotStyle.json";
import TrajectoryPoint from "../../../../model/TrajectoryPoint";
import ShipNotification from "../../../../model/ShipNotification";
import React from "react";
import PlotDataPointItem from "../../../../templates/PlotDataPointItem";
import "../../../../styles/object-details/scorePlot.css";
import { Stack } from "@mui/material";

interface ScorePlotProps {
  ship: ShipDetails;
  displayedTrajectoryAndNotifications: TrajectoryPoint[][];
  notifications: ShipNotification[];
}

function ScorePlot(props: ScorePlotProps) {
  const selectedShipId = props.ship.id;

  const threshold = plottingConfig.notificationThreshold;

  const allNotifications = props.notifications;

  const shipNotifications = allNotifications.filter((notification) => {
    return notification.shipDetails.id === selectedShipId;
  });

  const notificationScoreHistory = shipNotifications.map(
    (notification) => notification.shipDetails.anomalyScore,
  );
  console.log("NSCORE"+notificationScoreHistory);
  const notificationTimestampHistory = shipNotifications.map(
    (notification) => notification.shipDetails.timestamp,
  );
  console.log("NTIME"+notificationTimestampHistory);
  const shipHistory = props.displayedTrajectoryAndNotifications[0].map(
    (trajectoryPoint) => {
      return {
        anomalyScore: trajectoryPoint.anomalyScore,
        timestamp: trajectoryPoint.timestamp,
      } as PlotDataPointItem;
    },
  );

  const scoreHistory = shipHistory.map((dataPoint) => dataPoint.anomalyScore);
  const timestampHistory = shipHistory.map((dataPoint) => dataPoint.timestamp);
  console.log("SCORE"+scoreHistory);
  console.log("TIME"+timestampHistory);
  // Plot datapoint descriptions
  const anomalyScoreDescriptions = scoreHistory.map((score, index) => {
    return `Score: ${score}<br>Timestamp: ${timestampHistory[index]}`;
  });

  const notificationDescriptions = notificationScoreHistory.map(
    (score, index) => {
      return `Score: ${score}<br>Timestamp: ${notificationTimestampHistory[index]}`;
    },
  );

  return (
    <Stack className="plot-container">
      <Plot
        data={[
          {
            x: timestampHistory,
            y: scoreHistory,
            type: "scatter",
            mode: "lines",
            name: "Anomaly Scores",
            text: anomalyScoreDescriptions,
            hoverinfo: "text",
            line: { color: "blue" },
          },
          {
            x: notificationTimestampHistory,
            y: notificationScoreHistory,
            type: "scatter",
            mode: "markers",
            name: "Notifications",
            text: notificationDescriptions,
            hoverinfo: "text",
            marker: { color: "yellow", size: 8 },
          },
        ]}
        layout={{
          ...anomalyScorePlotStyle,
          xaxis: {
            title: "Timestamp",
            tickfont: { size: 6 },
            showticklabels: false
          },
          yaxis: {
            title: "Anomaly Score (%)",
            tickfont: { size: 6 },
          },
          shapes: [
            {
              type: "line",
              x0: timestampHistory[0],
              x1: timestampHistory[timestampHistory.length - 1],
              y0: threshold,
              y1: threshold,
              line: {
                color: "red",
                width: 2,
                dash: "dash",
              },
            },
          ],
        }}
      />
    </Stack>
  );
}

export default ScorePlot;
