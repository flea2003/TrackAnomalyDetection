import React from "react";
import Stack from "@mui/material/Stack";
import AnomalyListEntry from "./AnomalyListEntry";
import List from "@mui/material/List";
import { CurrentPage } from "../../../../App";
import ShipDetails from "../../../../model/ShipDetails";
import ShipThresholdModifier from "./AnomalyTitleWithSlider";

import "../../../../styles/common.css";
import "../../../../styles/anomaly-list/anomalyList.css";

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

  let noShipsEntry = <div> </div>;
  if (listEntries.length === 0) {
    noShipsEntry = <div className="no-ships-entry"> No ships </div>;
  }

  return (
    <Stack
      id="anomaly-list-container"
      data-testid="anomaly-list-container"
      direction="column"
    >
      <ShipThresholdModifier
        pageChanger={pageChanger}
        setFilterThreshold={setFilterThreshold}
        anomalyThreshold={anomalyThreshold}
      />
      {listEntries.length !== 0 ? (
        <List id="anomaly-list-internal-container">{listEntries}</List>
      ) : (
        noShipsEntry
      )}
    </Stack>
  );
}

export default AnomalyList;
