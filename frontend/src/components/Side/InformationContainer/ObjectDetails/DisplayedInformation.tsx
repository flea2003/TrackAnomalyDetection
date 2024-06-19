import React from "react";
import { useState } from "react";

import Stack from "@mui/material/Stack";
import ShipDetails from "../../../../model/ShipDetails";
import AnomalyDetails from "./AnomalyDetails";
import AISDetails from "./AISDetails";
import NotificationListWithoutTitle from "../NotificationsList/NotificationListWithoutTitle";
import ShipNotification from "../../../../model/ShipNotification";
import { CurrentPage } from "../../../../App";
import ScorePlot from "./ScorePlot";
import { ExtractedFunctionsMap } from "../../../Map/LMap";

import "../../../../styles/object-details/objectDetails.css";

interface ObjectDetailsProps {
  ship: ShipDetails;
  extractedFunctionsMap: React.RefObject<ExtractedFunctionsMap>;
  notifications: ShipNotification[];
  ships: ShipDetails[];
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
}

/**
 * Component that contains the menu and the data below the menu that is displayed
 * in object details window
 *
 * @param ship ship whose data is being displayed
 * @param extractedFunctionsMap reference of functions passed from the LMap components
 * @param notifications array of all notifications
 * @param ships array of all ships
 * @param pageChanger page changer function
 * @param mapCenteringFun map centering function
 * @constructor
 */
const DisplayedInformation = ({
  ship,
  extractedFunctionsMap,
  notifications,
  ships,
  pageChanger,
  mapCenteringFun,
}: ObjectDetailsProps) => {
  const [displayedAnomalyInfo, setDisplayedAnomalyInfo] = useState(true);
  const [displayedAIS, setDisplayedAIS] = useState(false);
  const [displayedNotifications, setDisplayedNotifications] = useState(false);
  const [displayedPlot, setDisplayedPlot] = useState(false);

  const changeAnomalyInfo = () => {
    setDisplayedAnomalyInfo(true);
    setDisplayedAIS(false);
    setDisplayedNotifications(false);
    setDisplayedPlot(false);
  };

  const changeAIS = () => {
    setDisplayedAnomalyInfo(false);
    setDisplayedAIS(true);
    setDisplayedNotifications(false);
    setDisplayedPlot(false);
  };

  const changeNotifications = () => {
    setDisplayedAnomalyInfo(false);
    setDisplayedAIS(false);
    setDisplayedNotifications(true);
    setDisplayedPlot(false);
  };

  const changePlot = () => {
    setDisplayedAnomalyInfo(false);
    setDisplayedAIS(false);
    setDisplayedNotifications(false);
    setDisplayedPlot(true);
  };

  const classnameIfAnomalyPlotIsDisplayed = displayedPlot
    ? " anomaly-plot-displayed"
    : "";

  return (
    <Stack
      direction="column"
      className={"menu-info-container" + classnameIfAnomalyPlotIsDisplayed}
    >
      <Stack direction="row" className="menu-container">
        <div
          onClick={changeAnomalyInfo}
          className={displayedAnomalyInfo ? "displayed" : "not-displayed"}
        >
          Information
        </div>
        <div
          onClick={changeAIS}
          className={displayedAIS ? "displayed" : "not-displayed"}
        >
          AIS
        </div>
        <div
          onClick={changeNotifications}
          className={displayedNotifications ? "displayed" : "not-displayed"}
        >
          Notifications
        </div>
        <div
          onClick={changePlot}
          className={displayedPlot ? "displayed" : "not-displayed"}
        >
          Plot
        </div>
      </Stack>
      <Stack className="info-container">
        {displayedAnomalyInfo && (
          <AnomalyDetails ship={ship} addAnomalyScore={false} />
        )}
        {displayedAIS && <AISDetails ship={ship} />}

        {displayedNotifications && notifications.length !== 0 && (
          <div className="notifications-container">
            <NotificationListWithoutTitle
              notifications={notifications}
              pageChanger={pageChanger}
              ships={ships}
              mapCenteringFun={mapCenteringFun}
            />
          </div>
        )}
        {displayedNotifications && notifications.length === 0 && (
          <div className="no-notifications">No notifications</div>
        )}
        {displayedPlot && (
          <div className="plot-container">
            <ScorePlot
              ship={ship}
              notifications={notifications}
              extractedFunctionsMap={extractedFunctionsMap}
            />
          </div>
        )}
      </Stack>
    </Stack>
  );
};

export default DisplayedInformation;
