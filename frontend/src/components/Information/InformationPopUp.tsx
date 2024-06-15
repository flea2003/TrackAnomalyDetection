import React, { useState } from "react";
import { CurrentPage } from "../../App";
import infoIcon from "../../assets/icons/helper-icons/info.svg";
import closeIcon from "../../assets/icons/helper-icons/close.svg";
import { informationText } from "./InformationText";

import "../../styles/common.css";
import "../../styles/informationPopUp.css";

interface InformationProps {
  currentPage: CurrentPage;
}

function InformationPopUp({ currentPage }: InformationProps) {
  const [isDisplayed, setDisplayed] = useState<boolean>(false);
  const changePopUpState = () => {
    setDisplayed((x) => !x);
  };

  const infoIconImage = (
    <img
      src={infoIcon}
      className="information-icon"
      alt="alt"
      onClick={changePopUpState}
    />
  );
  const closeIconImage = (
    <img
      src={closeIcon}
      className="close-icon"
      alt="alt"
      onClick={changePopUpState}
    />
  );

  if (isDisplayed) {
    return (
      <div>
        <div className="information-container">
          <div className="information-container-title">
            {getInfoTitle(currentPage.currentPage)}
          </div>
          <div className="information-text">
            {informationText(currentPage.currentPage)}
          </div>
          {closeIconImage}
        </div>
        {infoIconImage}
      </div>
    );
  } else return infoIconImage;
}

function getInfoTitle(page: string) {
  switch (page) {
    case "anomalyList":
      return "Anomaly List Information";
    case "objectDetails":
      return "Ship Details Information";
    case "notificationList":
      return "Notification List Information";
    case "notificationDetails":
      return "Notification Details Information";
    case "settings":
      return "Settings Information";
    case "errors":
      return "Error List Information";
    case "none":
      return "Map Information";
    default: {
      return "";
    }
  }
}

export default InformationPopUp;
