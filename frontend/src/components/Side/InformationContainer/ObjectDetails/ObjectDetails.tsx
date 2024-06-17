import React from "react";
import Stack from "@mui/material/Stack";
import ShipDetails from "../../../../model/ShipDetails";
import returnIcon from "../../../../assets/icons/helper-icons/back.svg";
import { CurrentPage } from "../../../../App";
import ShipNotification from "../../../../model/ShipNotification";
import { calculateAnomalyColor } from "../../../../utils/AnomalyColorCalculator";
import DisplayedInformation from "./DisplayedInformation";

import "../../../../styles/common.css";
import "../../../../styles/object-details/objectDetails.css";
import TrajectoryPoint from "../../../../model/TrajectoryPoint";

interface ObjectDetailsProps {
  ships: ShipDetails[];
  displayedTrajectoryAndNotifications: TrajectoryPoint[][];
  notifications: ShipNotification[];
  mapCenteringFun: (details: ShipDetails) => void;
  pageChanger: (currentPage: CurrentPage) => void;
  shipId: number;
}

/**
 * This component is the second column of the main view of the application. It displays the details of a selected object.
 * The object to whose details are to be displayed is passed as a prop.
 *
 * @param ships array of all ships
 * @param displayedTrajectoryAndNotifications historical data about the ship
 * @param notifications a list of all notifications
 * @param mapCenteringFun function used for map centering on a needed ship
 * @param pageChanger page changer function
 * @param shipId id of the ship
 * @constructor
 */
function ObjectDetails({
  ships,
  displayedTrajectoryAndNotifications,
  notifications,
  mapCenteringFun,
  pageChanger,
  shipId,
}: ObjectDetailsProps) {
  // Find the ship with the given ID in the map. If such ship is not (longer) present, show a message.
  const ship = ships.find((ship) => ship.id === shipId);
  const shipNotifications = notifications.filter(
    (x) => x.shipDetails.id === shipId,
  );

  if (ship === undefined) {
    return shipNotFoundElement();
  }

  return (
    <Stack className="object-details-container">
      <Stack className="object-details-title-container" direction="row">
        {getReturnIcon(pageChanger)}
        <Stack className="object-details-title">Ship #{ship.id}</Stack>
        <p
          className="object-anomaly-score"
          style={{ color: calculateAnomalyColor(ship.anomalyScore, true) }}
        >
          {" "}
          {ship.anomalyScore}%
        </p>
      </Stack>

      <DisplayedInformation
        ship={ship}
        displayedTrajectoryAndNotifications={
          displayedTrajectoryAndNotifications
        }
        notifications={shipNotifications}
        pageChanger={pageChanger}
        mapCenteringFun={mapCenteringFun}
        ships={ships}
      />
    </Stack>
  );
}

/**
 * Component that is displayed in case ship is not found
 */
function shipNotFoundElement() {
  return (
    <Stack className="object-details-container">
      <span className="object-details-title">
        Object ID:&nbsp;&nbsp;
        <span className="object-details-title-id">Not found</span>
      </span>
    </Stack>
  );
}

/**
 * Function that returns an icon for going back to anomaly list
 *
 * @param pageChanger page changer function
 */
function getReturnIcon(pageChanger: (currentPage: CurrentPage) => void) {
  const onReturnClicked = () => {
    pageChanger({ currentPage: "anomalyList", shownItemId: -1 });
  };

  const returnIconAlt = "Return Icon";

  return (
    <img
      src={returnIcon}
      className="object-details-return-icon"
      onClick={onReturnClicked}
      alt={returnIconAlt}
    />
  );
}

export default ObjectDetails;
