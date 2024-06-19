import React from "react";
import ShipDetails from "../../../../model/ShipDetails";
import Stack from "@mui/material/Stack";
import TimeUtilities from "../../../../utils/TimeUtilities";

import "../../../../styles/common.css";
import "../../../../styles/object-details/objectDetailsEntry.css";
import "../../../../styles/object-details/aisDetails.css";

interface ObjectDetailsProps {
  ship: ShipDetails;
}

/**
 * Function that returns the UI component that displays all AIS data for a ship
 *
 * @param ship ship whose data is being considered
 * @constructor
 */
function AISDetails({ ship }: ObjectDetailsProps) {
  return (
    <Stack direction="row" className="ais-details-container">
      <Stack direction="column" className="ais-details-column">
        <div className="ais-details-info-container">
          <div className="ais-details-subtitle">Last signal</div>
          <div className="ais-details-info">
            {TimeUtilities.getHoursAndMinutes(ship.timestamp)}
          </div>
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
          <div className="ais-details-info">{ship.speed} knots</div>
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
