import React, { CSSProperties } from "react";
import ShipDetails from "../../model/ShipDetails";
import "../../styles/shipIconDetails.css";
import { computeTimeDifference } from "../../utils/TimeUtilities";
interface ShipIconDetailsType {
  show: boolean;
  x: number;
  y: number;
  shipDetails: ShipDetails | null;
}

const ShipIconDetails = (props: ShipIconDetailsType) => {
  if (!props.show || props.shipDetails == null) {
    return null;
  } else {
    // Utility object for handling the injection of custom CSS properties
    const customStyle: CSSProperties = {
      "--top": `${props.y + 10}px`,
      "--left": `${props.x - 42.5}px`,
    } as CSSProperties & { [key: string]: string };

    return (
      <div className="shipIconDetails" style={customStyle}>
        <div>
          <strong>Ship ID: </strong>
          {props.shipDetails.id % 1000}
        </div>
        <div>
          <strong>Score: </strong>
          {props.shipDetails.anomalyScore}%
        </div>
        <div>
          <strong>Speed: </strong>
          {props.shipDetails.speed} kn
        </div>
        <div>
          <strong>Lag: </strong>
          {computeTimeDifference(props.shipDetails.timestamp)}
        </div>
      </div>
    );
  }
};

export default ShipIconDetails;
