import React from "react";
import ShipDetails from "../../model/ShipDetails";

interface ShipIconDetailsType {
  show: boolean;
  x: number;
  y: number;
  shipDetails: ShipDetails | null;
}

const ShipIconDetails = (props: ShipIconDetailsType) => {
  if (!props.show || props.shipDetails == null) {
    return;
  } else {
    return (
      <div
        className="shipIconDetails"
        style={{
          position: `relative`,
          top: `${props.y - 10}px`,
          left: `${props.x}px`,
          backgroundColor: `white`,
        }}
      >
        <p>
          <strong>ID: </strong>
          {props.shipDetails.id}
        </p>
        <p>
          <strong>Score: </strong>
          {props.shipDetails.anomalyScore}
        </p>
      </div>
    );
  }
};

export default ShipIconDetails;
