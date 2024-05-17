import React from "react";
import Stack from "@mui/material/Stack";
import List from "@mui/material/List";
import ShipDetails from "../../../model/ShipDetails";
import ObjectDetailsEntry from "./ObjectDetailsEntry";

import "../../../styles/common.css";
import "../../../styles/objectDetails.css";
import returnIcon from "../../../assets/icons/back.svg";
import { CurrentPage } from "../../../App";

interface ObjectDetailsProps {
  ships: ShipDetails[];
  shipId: string;
  pageChanger: (currentPage: CurrentPage) => void;
}

/**
 * This component is the second column of the main view of the application. It displays the details of a selected object.
 * The object to whose details are to be displayed is passed as a prop.
 *
 * @param props properties passed to this component. Most importantly, it contains the ship object whose details to display.
 */
function ObjectDetails(props: ObjectDetailsProps) {
  // Extract the props
  const allShips = props.ships;
  const shipID = props.shipId;
  const pageChanger = props.pageChanger;

  // Find the ship with the given ID in the map. If such ship is not (longer) present, show a message.
  const ship = allShips.find((ship) => ship.id === shipID);

  if (ship === undefined) {
    return shipNotFoundElement();
  }

  return (
    <Stack id="object-details-container">
      <div className="object-details-title-container">
        {getReturnIcon(pageChanger)}
        <span className="object-details-title">Score: {ship.anomalyScore} %</span>
      </div>
      <List
        style={{ maxHeight: "100%", overflow: "auto" }}
        className="object-details-list"
      >
        {getPropertyElements(ship)}
      </List>
    </Stack>
  );
}

function shipNotFoundElement() {
  return (
    <Stack id="object-details-container">
        <span className="object-details-title">
          Object ID:&nbsp;{" "}
          <span className="object-details-title-id">Not found</span>
        </span>
    </Stack>
  );
}

function getPropertyElements(ship: ShipDetails) {
  const properties = ship.getPropertyList();

  return properties.map((property) => {
    return (
      <ObjectDetailsEntry
        key={property.type}
        type={property.type}
        value={property.value}
      />
    );
  })
}

function getReturnIcon(pageChanger: (currentPage: CurrentPage) => void) {
  const onReturnClicked = () => {
    pageChanger({ currentPage: "anomalyList", shownShipId: "" });
  };

  const returnIconAlt = "Return Icon";

  return <img
    src={returnIcon}
    className="object-details-return-icon"
    onClick={onReturnClicked}
    alt={returnIconAlt}
  />
}

export default ObjectDetails;
