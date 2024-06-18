import React, { useState } from "react";
import { CurrentPage } from "../../App";
import infoIcon from "../../assets/icons/helper-icons/info.svg";
import closeIcon from "../../assets/icons/helper-icons/close.svg";
import { informationText } from "./InformationText";

import "../../styles/common.css";
import "../../styles/informationPopUp.css";

import "../../styles/common.css";
import "../../styles/informationPopUp.css";

interface InformationProps {
  currentPage: CurrentPage;
}

/**
 * Function that returns UI component for the information of the visual components that could be
 * seen at the current page. It is displayed in the botoom-left corner of the page, once the information
 * button is clicked.
 *
 * @param currentPage current page that is displayed in the application
 * @constructor
 */
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

/**
 * Function that returns the title for the information container.
 *
 * @param page name of the page that is currently being displayed
 */
function getInfoTitle(page: string) {
  switch (page) {
    case "anomalyList":
      return "Anomaly List";
    case "objectDetails":
      return "Ship Details";
    case "notificationList":
      return "Notification List";
    case "notificationDetails":
      return "Notification Details";
    case "settings":
      return "Settings";
    case "errors":
      return "Error List";
    case "none":
      return "Map";
    default: {
      return "";
    }
  }
}

export default InformationPopUp;
