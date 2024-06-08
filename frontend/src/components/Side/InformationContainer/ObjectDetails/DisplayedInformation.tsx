import React from "react";
import { useState } from "react";

import Stack from "@mui/material/Stack";

import "../../../../styles/ship-details/shipDetails.css";
import ShipDetails from "../../../../model/ShipDetails";
import AnomalyDetails from "./AnomalyDetails";
import AISDetails from "./AISDetails";
import NotificationListWithoutTitle from "../NotificationsList/NotificationListWithoutTitle";
import ShipNotification from "../../../../model/ShipNotification";
import { CurrentPage } from "../../../../App";

interface ObjectDetailsProps {
  ship: ShipDetails;
  notifications: ShipNotification[];
  ships: ShipDetails[];
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
}

const DisplayedInformation = ({ship, notifications, ships, pageChanger, mapCenteringFun}: ObjectDetailsProps) => {

  const [displayedAnomalyInfo, setDisplayedAnomalyInfo] = useState(true);
  const [displayedAIS, setDisplayedAIS] = useState(false);
  const [displayedNotifications, setDisplayedNotifications] = useState(false);
  const [displayedPlot, setDisplayedPlot] = useState(false);


  const changeAnomalyInfo = () => {
    setDisplayedAnomalyInfo(true);
    setDisplayedAIS(false)
    setDisplayedNotifications(false)
    setDisplayedPlot(false)
  };

  const changeAIS = () => {
    setDisplayedAnomalyInfo( false);
    setDisplayedAIS( true)
    setDisplayedNotifications( false)
    setDisplayedPlot(false)
  };

  const changeNotifications = () => {
    setDisplayedAnomalyInfo(false);
    setDisplayedAIS(false)
    setDisplayedNotifications(true)
    setDisplayedPlot(false)
  };

  const changePlot = () => {
    setDisplayedAnomalyInfo(false);
    setDisplayedAIS(false)
    setDisplayedNotifications(false)
    setDisplayedPlot(true)
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
        <div className="notifications-container">
          <NotificationListWithoutTitle
            notifications={notifications}
            pageChanger={pageChanger}
            ships={ships}
            mapCenteringFun={mapCenteringFun}
          />
        </div>
      )}
      {displayedPlot && (
        <div>
         Plot
        </div>
      )}
      </Stack>
    </Stack>
  );
};

export default DisplayedInformation;
