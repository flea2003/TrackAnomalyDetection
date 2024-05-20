import { CurrentPage } from "../../App";
import React, { JSX } from "react";
import ShipDetails from "../../model/ShipDetails";
import Sidebar from "./Sidebar/Sidebar";
import MiddleColumn from "./MiddleColumn/MiddleColumn";

import "../../styles/common.css";
import "../../styles/side.css";
import ErrorNotificationService from "../../services/ErrorNotificationService";

interface SideProps {
  currentPage: CurrentPage;
  ships: ShipDetails[];
  pageChanger: (currentPage: CurrentPage) => void;
  mapCenteringFun: (details: ShipDetails) => void;
}

function Side({
  currentPage,
  ships,
  pageChanger,
  mapCenteringFun,
}: SideProps): JSX.Element {
  // Set up the ErrorNotificationService
  const [, setErrorNotificationState] = React.useState(
    ErrorNotificationService.getAllNotifications(),
  );
  ErrorNotificationService.initialize(setErrorNotificationState);

  return (
    // <Stack className="side-container" direction="row">
    <>
      <MiddleColumn
        currentPage={currentPage}
        ships={ships}
        pageChanger={pageChanger}
        mapCenteringFun={mapCenteringFun}
      />
      <Sidebar pageChanger={pageChanger} />
    </>
    // </Stack>
  );
}

export default Side;
