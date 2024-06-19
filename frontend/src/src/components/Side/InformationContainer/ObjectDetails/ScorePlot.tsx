import ShipDetails from "../../../../model/ShipDetails";
import plottingConfig from "../../../../configs/plottingConfig.json";
import Plot from "react-plotly.js";
import anomalyScorePlotStyle from "../../../../configs/anomalyScorePlotStyle.json";
import ShipNotification from "../../../../model/ShipNotification";
import React from "react";
import PlotDataPointItem from "../../../../templates/PlotDataPointItem";
import "../../../../styles/object-details/scorePlot.css";
import TrajectoryAndNotificationPair from "../../../../model/TrajectoryAndNotificationPair";

interface ScorePlotProps {
  ship: ShipDetails;
  displayedTrajectoryAndNotifications: TrajectoryAndNotificationPair;
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

  const notificationTimestampHistory = shipNotifications.map(
    (notification) => new Date(notification.shipDetails.timestamp),
  );

  const rawTrajectoryData = props.displayedTrajectoryAndNotifications.trajectory;
  if (rawTrajectoryData.length === 0 || rawTrajectoryData === undefined) {
    return (
    <div>
      Waiting for data...
    </div>
    );
  }

  const shipHistory =  preprocessHistory(
    props.displayedTrajectoryAndNotifications,
  );

  const scoreHistory = shipHistory.map((dataPoint) => dataPoint.anomalyScore);
  const timestampHistory = shipHistory.map(
    (dataPoint) => new Date(dataPoint.timestamp),
  );

  // Plot datapoint descriptions
  const anomalyScoreDescriptions = scoreHistory.map((score, index) => {
    return `Score: ${score}<br>Timestamp: ${timestampHistory[index].toLocaleString()}`;
  });

  const notificationDescriptions = notificationScoreHistory.map(
    (score, index) => {
      return `Score: ${score}<br>Timestamp: ${notificationTimestampHistory[index].toLocaleString()}`;
    },
  );

  return (
    <div className="plot-container">
      <Plot
        data={[
          {
            x: timestampHistory.map((val) => val.getTime()),
            y: scoreHistory,
            type: "scatter",
            mode: "lines+markers",
            name: "Anomaly Score",
            text: anomalyScoreDescriptions,
            hoverinfo: "text",
            hoverlabel: {
              // Customizing hover label for a specific trace
              bgcolor: "#bbbbc3", // Background color
              bordercolor: "#4c4949", // Border color
              font: {
                size: 8,
                color: "#2e2b2b",
              },
            },
            line: { color: "#63aaba"},
            marker: {
              color: "#63aaba",
              size: 3,
              symbol: "circle",
            },
          },
          {
            x: notificationTimestampHistory.map((val) => val.getTime()),
            y: notificationScoreHistory,
            type: "scatter",
            mode: "markers",
            name: "Notifications",
            text: notificationDescriptions,
            hoverinfo: "text",
            hoverlabel: {
              // Customizing hover label for a specific trace
              bgcolor: "#e9f3b3", // Background color
              bordercolor: "#4c4949", // Border color
              font: {
                size: 8,
                color: "#2e2b2b",
              },
            },
            marker: {
              color: "#ff7f27",
              size: 3,
              symbol: "circle",
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
            b: 30,
          },
          xaxis: {
            title: "Timestamp",
            titlefont: {
              size: 10,
            },
            tickfont: { size: 6 },
            showticklabels: true,
            tickmode: "array",
            tickvals: [
              timestampHistory[0].getTime(),
              timestampHistory[timestampHistory.length - 1].getTime(),
            ],
            ticktext: [
              timestampHistory[0].toLocaleString(),
              timestampHistory[timestampHistory.length - 1].toLocaleString(),
            ],
            showgrid: false,
            range: [
              timestampHistory[0].getTime() - 1000 * 50,
              timestampHistory[timestampHistory.length - 1].getTime() + 1000 * 10,
            ],
          },
          yaxis: {
            range: [0, 100],
            tickfont: { size: 6 },
          },
          legend: {
            // change orientation 
            orientation: "v",
            x: 0.5, // Center the legend horizontally
            y: 0.9, // Position above the top of the plot area
            xanchor: "center", // Anchor the legend at its center
            yanchor: "bottom", // Anchor the legend just below the specified 'y' position
          },
          shapes: [
            {
              type: "line",
              x0: timestampHistory[0].getTime(),
              x1: timestampHistory[timestampHistory.length - 1].getTime(),
              y0: threshold,
              y1: threshold,
              line: {
                color: "#ff7f27",
                width: 1,
                dash: "dash",
              },
            },
          ],
        }}
        useResizeHandler={true}
        config={{ displayModeBar: false }}
      />
    </div>
  );
}

const preprocessHistory = (trajectoryData: TrajectoryAndNotificationPair) => {
  const parsedFilteredData = trajectoryData.trajectory
    .map((trajectoryPoint) => {
      return {
        anomalyScore: trajectoryPoint.anomalyScore,
        timestamp: new Date(trajectoryPoint.timestamp),
      } as PlotDataPointItem;
    })
    .filter((trajectoryPnt) => trajectoryPnt.anomalyScore !== -1);

  // Remove duplicate instances
  const uniqueDataPoints = Array.from(
    new Set(parsedFilteredData.map((obj) => JSON.stringify(obj))),
  ).map((json) => JSON.parse(json) as PlotDataPointItem);

  return uniqueDataPoints.sort((p1, p2) => {
    if (p1.timestamp < p2.timestamp) {
      return -1;
    }
    if (p1.timestamp === p2.timestamp) {
      return 0;
    } else {
      return 1;
    }
  });
};


export default ScorePlot;
