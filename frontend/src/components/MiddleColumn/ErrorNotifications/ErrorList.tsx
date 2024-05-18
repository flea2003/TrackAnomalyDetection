import React from "react";
import Stack from "@mui/material/Stack";
import ErrorListEntry from "./ErrorListEntry";
import List from "@mui/material/List";
import { CurrentPage } from "../../../App";
import closeIcon from "../../../assets/icons/close.svg";

import "../../../styles/common.css";
import "../../../styles/errorList.css";
import ErrorNotificationService, {
  ErrorNotification,
} from "../../../services/ErrorNotificationService";

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
function ErrorList({ pageChanger }: ErrorListProps) {
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

        <button
          id="error-list-mark-all-button"
          data-testid="error-list-mark-all-button"
          onClick={() => ErrorNotificationService.markAllAsRead()}
        >
          Mark all as read
        </button>
      </Stack>
      <List
        id="error-list-internal-container"
        style={{ maxHeight: "100%", overflow: "auto", padding: "0" }}
      >
        {getErrorListEntries()}
      </List>
    </Stack>
  );
}

/**
 * Gets all error notifications, reverses them (so that the newest one is the first
 * one), and returns a list of corresponding ErrorListEntry.
 */
function getErrorListEntries() {
  return ErrorNotificationService.getAllNotifications()
    .slice()
    .reverse()
    .map((notification: ErrorNotification, i: number) => (
      <ErrorListEntry key={i} notification={notification} />
    ));
}

export default ErrorList;
