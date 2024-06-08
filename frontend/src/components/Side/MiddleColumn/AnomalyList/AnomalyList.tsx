import React from "react";
import Stack from "@mui/material/Stack";
import AnomalyListEntry from "./AnomalyListEntry";
import List from "@mui/material/List";
import { CurrentPage } from "../../../../App";
import ShipDetails from "../../../../model/ShipDetails";
import ShipThresholdModifier from "./AnomalyTitleWithSlider";

import "../../../../styles/common.css";
import "../../../../styles/anomalyList.css";

interface AnomalyListProps {
  ships: ShipDetails[];
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
  setFilterThreshold: (value: number) => void;
  anomalyThreshold: number;
}

/**
 * This component is the second column of the main view of the application. It essentially displays
 * the "Anomaly List" title and renders all the AnomalyListEntry components.
 *
 * @param ships a list of ships to display in the list
 * @param pageChanger function that, when called, changes the page displayed in the second column.
 * @param mapCenteringFun function that, when called, centers the map on a specific ship
 * @param setFilterThreshold function that sets the filtering threshold
 * @param anomalyThreshold the anomaly threshold that is used for filtering
 */
function AnomalyList({
  ships,
  pageChanger,
  mapCenteringFun,
  setFilterThreshold,
  anomalyThreshold,
}: AnomalyListProps) {
  const listEntries = ships.slice(0, 250).map((ship, id) =>
    <AnomalyListEntry
      key={id}
      shipDetails={ship}
      pageChanger={pageChanger}
      mapCenteringFun={mapCenteringFun}
    />
  );

  return (
    <Stack id="anomaly-list-container" data-testid="anomaly-list-container">
      <ShipThresholdModifier
        pageChanger={pageChanger}
        setFilterThreshold={setFilterThreshold}
        anomalyThreshold={anomalyThreshold}
      />
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
