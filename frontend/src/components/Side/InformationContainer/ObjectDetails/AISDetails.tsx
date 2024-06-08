import React from "react";

import "../../../../styles/common.css";
import "../../../../styles/ship-details/shipDetailsEntry.css";
import ShipDetails from "../../../../model/ShipDetails";
import ShipNotification from "../../../../model/ShipNotification";
import { CurrentPage } from "../../../../App";

import "../../../../styles/ship-details/aisDetails.css";
import TimeUtilities from "../../../../utils/TimeUtilities";
import Stack from "@mui/material/Stack";

interface ObjectDetailsProps {
  ship: ShipDetails;
}

function AISDetails({ship} : ObjectDetailsProps) {
  return (
    <Stack direction="row" className="ais-details-container">
      <Stack direction="column" className="ais-details-column">
        <div className="ais-details-info-container">
          <div className="ais-details-subtitle">Last signal</div>
          <div className="ais-details-info">{TimeUtilities.getHoursAndMinutes(ship.timestamp)}</div>
        </div>
        <div className="ais-details-info-container">
          <div className="ais-details-subtitle">Course</div>
          <div className="ais-details-info">{ship.course}°</div>
        </div>
        <div className="ais-details-info-container">
          <div className="ais-details-subtitle">Latitude</div>
          <div className="ais-details-info">{ship.getRoundedLatitude()}</div>
        </div>
        <div className="ais-details-info-container">
          <div className="ais-details-subtitle">Speed</div>
          <div className="ais-details-info">{ship.speed} km/min</div>
        </div>
      </Stack>
      <Stack direction="column" className="ais-details-column">
        <div className="ais-details-info-container">
          <div className="ais-details-subtitle">Departure Port</div>
          <div className="ais-details-info">{ship.departurePort}</div>
        </div>
        <div className="ais-details-info-container">
          <div className="ais-details-subtitle">Heading</div>
          <div className="ais-details-info">{ship.heading}°</div>
        </div>
        <div className="ais-details-info-container">
          <div className="ais-details-subtitle">Longitude</div>
          <div className="ais-details-info">{ship.getRoundedLongitude()}</div>
        </div>
      </Stack>
    </Stack>
  );
}

export default AISDetails;
