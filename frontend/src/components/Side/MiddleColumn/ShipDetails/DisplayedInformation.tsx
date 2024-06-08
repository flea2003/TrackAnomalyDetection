import React from "react";
import { useState } from "react";

import Stack from "@mui/material/Stack";

import "../../../../styles/ship-details/shipDetails.css";
import ShipDetails from "../../../../model/ShipDetails";
import AnomalyDetails from "./AnomalyDetails";
import AISDetails from "./AISDetails";
import NotificationListWithoutTitle from "../NotificationsList/NotificationListWithoutTitle";

interface ObjectDetailsProps {
  ship: ShipDetails;
}


const DisplayedInformation = ({ship}: ObjectDetailsProps) => {

  const [displayedAnomalyInfo, setDisplayedAnomalyInfo] = useState(true);
  const [displayedAIS, setDisplayedAIS] = useState(false);
  const [displayedNotifications, setDisplayedNotifications] = useState(false);
  const [displayedPlot, setDisplayedPlot] = useState(false);


  const changeAnomalyInfo = () => {
    setDisplayedAnomalyInfo((x) => true);
    setDisplayedAIS((x) => false)
    setDisplayedNotifications((x) => false)
    setDisplayedPlot((x) => false)
  };

  const changeAIS = () => {
    setDisplayedAnomalyInfo((x) => false);
    setDisplayedAIS((x) => true)
    setDisplayedNotifications((x) => false)
    setDisplayedPlot((x) => false)
  };

  const changeNotifications = () => {
    setDisplayedAnomalyInfo((x) => false);
    setDisplayedAIS((x) => false)
    setDisplayedNotifications((x) => true)
    setDisplayedPlot((x) => false)
  };

  const changePlot = () => {
    setDisplayedAnomalyInfo((x) => false);
    setDisplayedAIS((x) => false)
    setDisplayedNotifications((x) => false)
    setDisplayedPlot((x) => true)
  };

  return (
    <Stack direction="column" className="menu-info-container">
      <Stack direction="row" className="menu-container">
        <div onClick={changeAnomalyInfo} className={displayedAnomalyInfo ? "displayed" : "not-displayed"}>
          Information
        </div>
        <div onClick={changeAIS} className={displayedAIS ? "displayed" : "not-displayed"}>
          AIS
        </div>
        <div onClick={changeNotifications} className={displayedNotifications ? "displayed" : "not-displayed"}>
          Notifications
        </div>
        <div onClick={changePlot} className={displayedPlot ? "displayed" : "not-displayed"}>
          Plot
        </div>
      </Stack>
      <Stack className="info-container">
      {displayedAnomalyInfo && (
        <AnomalyDetails ship={ship}/>
      )}

        {displayedAIS && (
        <AISDetails ship={ship}/>
      )}

      {displayedNotifications && (
        <span>
         {/*<NotificationListWithoutTitle />*/}
        </span>
      )}

      {displayedPlot && (
        <span>
         Plot
        </span>
      )}
      </Stack>

    </Stack>
  );
};

export default DisplayedInformation;
