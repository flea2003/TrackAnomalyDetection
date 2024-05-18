import { CurrentPage } from "../../App";
import AnomalyList from "./AnomalyList/AnomalyList";
import ObjectDetails from "./ObjectDetails/ObjectDetails";
import React, { JSX } from "react";
import ShipDetails from "../../model/ShipDetails";
import ErrorList from "./ErrorNotifications/ErrorList";
import ErrorNotificationService from "../../services/ErrorNotificationService";

interface MiddleColumnProps {
  currentPage: CurrentPage;
  ships: ShipDetails[];
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
}

function MiddleColumn({
  currentPage,
  ships,
  pageChanger,
  mapCenteringFun,
}: MiddleColumnProps): JSX.Element {
  switch (currentPage.currentPage) {
    case "anomalyList":
      return (
        <AnomalyList
          ships={ships}
          pageChanger={pageChanger}
          mapCenteringFun={mapCenteringFun}
        />
      );
    case "objectDetails":
      return (
        <ObjectDetails
          ships={ships}
          shipId={currentPage.shownShipId}
          pageChanger={pageChanger}
        />
      );
    case "notifications":
      return <div>Notifications</div>;
    case "settings":
      return <div>Settings</div>;
    case "errors":
      return <ErrorList pageChanger={pageChanger} />;
    case "none":
      return <div></div>;
    default: {
      ErrorNotificationService.addWarning(
        "MiddleColumn required page not found: " + currentPage.currentPage,
      );
      return <div></div>;
    }
  }
}

export default MiddleColumn;
