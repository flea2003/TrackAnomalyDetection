import { CurrentPage } from "../../App";
import React, { JSX } from "react";
import ShipDetails from "../../model/ShipDetails";
import Sidebar from "./Sidebar/Sidebar";
import MiddleColumn from "./MiddleColumn/MiddleColumn";
import ErrorNotificationService from "../../services/ErrorNotificationService";
import ShipNotification from "../../model/ShipNotification";

import "../../styles/common.css";
import "../../styles/side.css";

interface SideProps {
  currentPage: CurrentPage;
  ships: ShipDetails[];
  notifications: ShipNotification[];
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
}

function Side({
  currentPage,
  ships,
  notifications,
  pageChanger,
  mapCenteringFun,
}: SideProps): JSX.Element {
  // Set up the ErrorNotificationService
  const [, setErrorNotificationState] = React.useState(
    ErrorNotificationService.getAllNotifications(),
  );
  ErrorNotificationService.initialize(setErrorNotificationState);

  return (
    <>
      <MiddleColumn
        currentPage={currentPage}
        ships={ships}
        notifications={notifications}
        pageChanger={pageChanger}
        mapCenteringFun={mapCenteringFun}
      />
      <Sidebar pageChanger={pageChanger} />
    </>
    // </Stack>
  );
}

export default Side;
