import ShipDetails from "../../../../model/ShipDetails";
import plottingConfig from "../../../../configs/plottingConfig.json";
import Plot from "react-plotly.js";
import anomalyScorePlotStyle from "../../../../configs/anomalyScorePlotStyle.json";
import ShipNotification from "../../../../model/ShipNotification";
import React from "react";
import PlotDataPointItem from "../../../../templates/PlotDataPointItem";
import "../../../../styles/object-details/scorePlot.css";
import { ExtractedFunctionsMap } from "../../../Map/LMap";

interface ScorePlotProps {
  ship: ShipDetails;
  extractedFunctionsMap: React.RefObject<ExtractedFunctionsMap>;
  notifications: ShipNotification[];
}

/**
 *
 * @param ship ship whose data is being dispalyed
 * @param extractedFunctionsMap reference of functions passed from the LMap component
 * @param notifications all notification history stored in frontend
 * @constructor
 */
function ScorePlot({
  ship,
  extractedFunctionsMap,
  notifications,
}: ScorePlotProps) {
  const selectedShipId = ship.id;

  const threshold = plottingConfig.notificationThreshold;

  const shipNotifications = notifications.filter((notification) => {
    return notification.shipDetails.id === selectedShipId;
  });

  const notificationScoreHistory = shipNotifications.map(
    (notification) => notification.shipDetails.anomalyScore,
  );
  console.log("NSCORE:{" + notificationScoreHistory + "}");
  const notificationTimestampHistory = shipNotifications.map(
    (notification) => new Date(notification.shipDetails.timestamp),
  );
  console.log("NTIME:{" + notificationTimestampHistory + "}");

  const shipHistory = preprocessHistory(extractedFunctionsMap);

  const scoreHistory = shipHistory.map((dataPoint) => dataPoint.anomalyScore);
  const timestampHistory = shipHistory.map(
    (dataPoint) => new Date(dataPoint.timestamp),
  );

  console.log("SCORE:{" + scoreHistory + "}");
  console.log("TIME:{" + timestampHistory + "}");
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
            line: { color: "blue" },
            marker: {
              color: "blue",
              size: 5,
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
              color: "yellow",
              size: 5,
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
              timestampHistory[0].getTime(),
              timestampHistory[timestampHistory.length - 1].getTime(),
            ],
          },
          yaxis: {
            range: [0, 100],
            tickfont: { size: 6 },
          },
          legend: {
            orientation: "h",
            x: 0.5, // Center the legend horizontally
            y: 1.1, // Position above the top of the plot area
            xanchor: "center", // Anchor the legend at its center
            yanchor: "bottom", // Anchor the legend just below the specified 'y' position
          },
          shapes: [
            {
              type: "line",
              x0: scoreHistory[0],
              x1: scoreHistory[scoreHistory.length - 1],
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
        config={{ displayModeBar: false }}
      />
    </div>
  );
}

/**
 * Function that turns the TrajectoryAndNotification object to a needed array
 * for plotting
 *
 * @param extractedFunctionsMap function dictionary that contains reference to the trajectory object
 */
const preprocessHistory = (
  extractedFunctionsMap: React.RefObject<ExtractedFunctionsMap>,
) => {
  if (extractedFunctionsMap.current === null) return [];

  const parsedFilteredData =
    extractedFunctionsMap.current.displayedTrajectoryAndNotifications.trajectory
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
