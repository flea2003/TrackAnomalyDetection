import React from "react";
import Stack from "@mui/material/Stack";
import ErrorListEntry from "./ErrorListEntry";
import List from "@mui/material/List";
import { CurrentPage } from "../../../App";
import closeIcon from "../../../assets/icons/close.svg";

import "../../../styles/common.css";
import "../../../styles/anomalyList.css";

interface ErrorListProps {
  pageChanger: (currentPage: CurrentPage) => void;
}

/**
 * This component is the second column of the main view of the application.
 * It displays the (software) errors (or warnings, info) that were caught in our
 * code and passed to ErrorNotificationService.
 *
 * @param pageChanger function that, when called, changes the page displayed in the second column.
 */
function ErrorList({
  pageChanger
}: ErrorListProps) {
  const listEntries = [];
  for (let i = 0; i < ships.length; i++) {
    listEntries.push(
      <ErrorListEntry
        key={i}
        shipDetails={ships[i]}
        pageChanger={pageChanger}
        mapCenteringFun={mapCenteringFun}
      />,
    );
  }

  return (
    <Stack id="error-list-container" data-testid="error-list-container">
      <Stack id="error-list-title-container" direction="row">
        <img
          src={closeIcon}
          alt="Close"
          id="error-list-close-icon"
          data-testid="error-list-close-icon"
          onClick={() => pageChanger({ currentPage: "none", shownShipId: -1 })}
        />
      </Stack>
      <List
        id="error-list-internal-container"
        style={{ maxHeight: "100%", overflow: "auto", padding: "0" }}
      >
        {listEntries}
      </List>
    </Stack>
  );
}

export default ErrorList;
