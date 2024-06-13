import React, { useState } from "react";
import { CurrentPage } from "../../App";
import "../../styles/informationPopUp.css";
import infoIcon from "../../assets/icons/helper-icons/info.svg"
import closeIcon from "../../assets/icons/helper-icons/close.svg"


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
      className="info-icon"
      alt="sth"
      onClick={changePopUpState}
      />
    );
  const closeIconImage = (<img
    src={closeIcon}
    className="close-icon"
    alt="sth"
    onClick={changePopUpState}
  />);

  if (isDisplayed) {
    return (
      <div>
        <div className="info-container">
          <div className="info-container-title">{getInfoTitle(currentPage.currentPage)}</div>
          {getInfoExplanation(currentPage.currentPage)}
          {closeIconImage}
        </div>
        {infoIconImage}
      </div>
    )
    } else return infoIconImage

}

function getInfoExplanation(page: string) {
  switch (page) {
    case "anomalyList":
      return "You are viewing anomaly list pagefdlkgjsssssssssssssssssssssssssssssssssssssssssssssssssdsfkjbvsdkjflbgklsjdbfgkljsdfgbkjl";
    case "objectDetails":
      return "you are viewing object details page";
    case "notificationList":
      return "You are viewing anomaly list page";
    case "notificationDetails":
      return "You are viewing anomaly list pagefdlkgjsssssssssssssssssssssssssssssssssssssssssssssssssdsfkjbvsdkjflbgklsjdbfgkljsdfgbkjl";
    case "settings":
      return "You are viewing anomaly list page";
    case "errors":
      return "You are viewing anomaly list page";
    case "none":
      return "You are viewing the map";
    default: {
      return "";
    }
  }
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
