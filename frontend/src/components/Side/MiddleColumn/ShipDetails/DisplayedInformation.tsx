import React from "react";
import { useState } from "react";

import Stack from "@mui/material/Stack";

import "../../../../styles/anomaly-list/anomalyList.css";
import "../../../../styles/anomaly-list/slider.css";

const DisplayedInformation = () => {

  const [displayedAnomalyInfo, setDisplayedAnomalyInfo] = useState(true);
  const [displayedAIS, setDisplayedAIS] = useState(false);
  const [displayedNotifications, setDisplayedNotifications] = useState(false);
  const [displayedPlot, setDisplayedPlot] = useState(false);


  const changeAnomalyInfo = () => {
    setDisplayedAnomalyInfo((prevState) => true);
  };

  const changeAIS = () => {
    setDisplayedAIS((prevState) => true);
  };

  const changeNotifications = () => {
    setDisplayedNotifications((prevState) => true);
  };

  const changePlot = () => {
    setDisplayedPlot((prevState) => true);
  };

  return (
    <Stack direction="column">
      <Stack direction="row">
        <div onClick={changeAnomalyInfo} className={displayedAnomalyInfo ? "displayed" : "not-displayed"}>
          Anomaly Info
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
      {displayedAnomalyInfo && (
        <span>
         Anomaly info data
        </span>
      )}

      {displayedAIS && (
        <span>
         AIS data
        </span>
      )}

      {displayedNotifications && (
        <span>
         Notifications data
        </span>
      )}

      {displayedPlot && (
        <span>
         Plot
        </span>
      )}

    </Stack>
  );
};

export default DisplayedInformation;
