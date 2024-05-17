import React from "react";
import Stack from "@mui/material/Stack";

import ShipDetails from "../../../model/ShipDetails";
import { calculateAnomalyColor } from "../../../utils/AnomalyColorCalculator";
import shipIcon from "../../../assets/icons/ship.png";

import "../../../styles/common.css";
import "../../../styles/anomalyListEntry.css";
import { CurrentPage } from "../../../App";

interface AnomalyListEntryProps {
  shipDetails: ShipDetails;
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
}

/**
 * This component is a single entry in the Anomaly List. It displays the anomaly score and an icon of an object.
 * The object to render is passed as a prop.
 *
 * @param shipDetails the specific details of the ship to display
 * @param pageChanger function that, when called, changes the page displayed in the second column.
 * @param mapCenteringFun function that, when called, centers the map on a specific ship
 */
function AnomalyListEntry({
  shipDetails,
  pageChanger,
  mapCenteringFun,
}: AnomalyListEntryProps) {
  const shipIconAltText = "Ship Icon";

  const shipAnomalyScore = shipDetails.anomalyScore;
  const color = calculateAnomalyColor(shipAnomalyScore);

  const onClick = () => {
    pageChanger({ currentPage: "objectDetails", shownShipId: shipDetails.id });
    mapCenteringFun(shipDetails);
  };

  return (
    <Stack
      direction="row"
      className="anomaly-list-entry"
      spacing={0}
      style={{ backgroundColor: color }}
      onClick={onClick}
    >
      <span className="anomaly-list-entry-icon-container">
        <img
          src={shipIcon}
          className="anomaly-list-entry-icon"
          alt={shipIconAltText}
        />
      </span>
      <span className="anomaly-list-entry-score">
        100 %
        {/*{shipAnomalyScore} %*/}
      </span>
    </Stack>
  );
}

export default AnomalyListEntry;
