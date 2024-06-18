import ShipDetails from "../../../../model/ShipDetails";
import plottingConfig from "../../../../configs/plottingConfig.json";
import Plot from "react-plotly.js";
import anomalyScorePlotStyle from "../../../../configs/anomalyScorePlotStyle.json";
import TrajectoryPoint from "../../../../model/TrajectoryPoint";
import ShipNotification from "../../../../model/ShipNotification";
import React from "react";
import PlotDataPointItem from "../../../../templates/PlotDataPointItem";
import "../../../../styles/object-details/scorePlot.css";


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
  console.log("NSCORE:{"+notificationScoreHistory+"}");
  const notificationTimestampHistory = shipNotifications.map(
    (notification) => new Date(notification.shipDetails.timestamp).toLocaleDateString(),
  );
  console.log("NTIME:{"+notificationTimestampHistory+"}");

  const shipHistory = preprocessHistory(props.displayedTrajectoryAndNotifications);

  const scoreHistory = shipHistory.map((dataPoint) => dataPoint.anomalyScore);
  const timestampHistory = shipHistory.map((dataPoint) => new Date(dataPoint.timestamp).toLocaleDateString());

  console.log("SCORE:{"+scoreHistory+"}");
  console.log("TIME:{"+timestampHistory+"}");
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
    <div className="plot-container">
      <Plot
        data={[
          {
            x: timestampHistory,
            y: scoreHistory,
            type: "scatter",
            mode: "lines+markers",
            name: "Anomaly Score",
            text: anomalyScoreDescriptions,
            hoverinfo: "text",
            hoverlabel: { // Customizing hover label for a specific trace
              bgcolor: "#bbbbc3", // Background color
              bordercolor: "#4c4949", // Border color
              font: {
                size: 8,
                color: "#2e2b2b"
              }
            },
            line: { color: "blue" },
            marker: {
              color: "blue",
              size: 5,
              symbol: "circle"
            }
          },
          {
            x: notificationTimestampHistory,
            y: notificationScoreHistory,
            type: "scatter",
            mode: "markers",
            name: "Notifications",
            text: notificationDescriptions,
            hoverinfo: "text",
            hoverlabel: { // Customizing hover label for a specific trace
              bgcolor: "#e9f3b3", // Background color
              bordercolor: "#4c4949", // Border color
              font: {
                size: 8,
                color: "#2e2b2b"
              }
            },
            marker: {
              color: "yellow",
              size: 5,
              symbol: "square"
            },
          },
        ]}
        layout={{
          ...anomalyScorePlotStyle,
          autosize: true,
          margin: {
            r: 40,
            l: 40,
            t: 30,
            b: 30
          },
          xaxis: {
            title: "Timestamp",
            titlefont: {
              size: 10
            },
            tickfont: { size: 6 },
            showticklabels: true,
            tickmode: 'array',
            tickvals: [timestampHistory[0], timestampHistory[timestampHistory.length - 1]],
            ticktext: [
              timestampHistory[0],
              timestampHistory[timestampHistory.length - 1]
            ],
            showgrid: false
          },
          yaxis: {
            tickfont: { size: 6 },
          },
          legend: {
            orientation: "h",
            x: 0.5, // Center the legend horizontally
            y: 1.1, // Position above the top of the plot area
            xanchor: 'center', // Anchor the legend at its center
            yanchor: 'bottom' // Anchor the legend just below the specified 'y' position
          },
          shapes: [
            {
              type: "line",
              x0: scoreHistory[0],
              x1: scoreHistory[scoreHistory.length-1],
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
        useResizeHandler={true}
        config={{displayModeBar: false}}
      />
    </div>
  );
}

const preprocessHistory = (trajectoryData: TrajectoryPoint[][]) => {
  const parsedFilteredData = trajectoryData[0].map(
    (trajectoryPoint) => {
      return {
        anomalyScore: trajectoryPoint.anomalyScore,
        timestamp: trajectoryPoint.timestamp,
      } as PlotDataPointItem;
    },
  ).filter(trajectoryPnt => trajectoryPnt.anomalyScore !== -1);

  // Remove duplicate instances
  return Array.from(
    new Set(parsedFilteredData.map(obj => JSON.stringify(obj)))
  ).map(json => JSON.parse(json) as PlotDataPointItem);
}

function arePropsEqual(prevProps: ScorePlotProps, nextProps: ScorePlotProps) {
  const sameShip = prevProps.ship.id === nextProps.ship.id;

  const sameTrajectoryInfo = prevProps.displayedTrajectoryAndNotifications === nextProps.displayedTrajectoryAndNotifications ||
    (prevProps.displayedTrajectoryAndNotifications.length === nextProps.displayedTrajectoryAndNotifications.length &&
      prevProps.displayedTrajectoryAndNotifications.every((item, index) =>
        item.length === nextProps.displayedTrajectoryAndNotifications[index].length &&
        item.every((point, ptIndex) =>
          point === nextProps.displayedTrajectoryAndNotifications[index][ptIndex])));

  const sameNotifications = prevProps.notifications === nextProps.notifications ||
    (prevProps.notifications.length === nextProps.notifications.length &&
      prevProps.notifications.every((item, index) => item.id === nextProps.notifications[index].id));

  return sameShip && sameTrajectoryInfo && sameNotifications;
}

export default React.memo(ScorePlot, arePropsEqual);
