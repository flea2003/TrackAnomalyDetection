import React from "react";
import Stack from "@mui/material/Stack";
import AnomalyListEntry from "./AnomalyListEntry";
import List from "@mui/material/List";
import { CurrentPage } from "../../../App";
import ShipDetails from "../../../model/ShipDetails";
import closeIcon from "../../../assets/icons/close.svg";

import "../../../styles/common.css";
import "../../../styles/anomalyList.css";

interface AnomalyListProps {
  ships: ShipDetails[];
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
}

/**
 * This component is the second column of the main view of the application. It essentially displays
 * the "Anomaly List" title and renders all the AnomalyListEntry components.
 *
 * @param ships a list of ships to display in the list
 * @param pageChanger function that, when called, changes the page displayed in the second column.
 * @param mapCenteringFun function that, when called, centers the map on a specific ship
 */
function AnomalyList({
  ships,
  pageChanger,
  mapCenteringFun,
}: AnomalyListProps) {
  const listEntries = [];
  for (let i = 0; i < ships.length; i++) {
    listEntries.push(
      <AnomalyListEntry
        key={i}
        shipDetails={ships[i]}
        pageChanger={pageChanger}
        mapCenteringFun={mapCenteringFun}
      />,
    );
  }

  return (
    <Stack id="anomaly-list-container">
      <Stack id="anomaly-list-title-container" direction="row">
        <img
          src={closeIcon}
          alt="Close"
          id="anomaly-list-close-icon"
          data-testid="anomaly-list-close-icon"
          onClick={() => pageChanger({ currentPage: "none", shownShipId: "" })}
        />
      </Stack>
      <List
        id="anomaly-list-internal-container"
        style={{ maxHeight: "100%", overflow: "auto", padding: "0" }}
      >
        {listEntries}
      </List>
    </Stack>
  );
}

export default AnomalyList;
