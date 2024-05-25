import { CurrentPage } from "../../../../App";
import Stack from "@mui/material/Stack";
import closeIcon from "../../../../assets/icons/close.svg";
import ErrorNotificationService, { ErrorNotification } from "../../../../services/ErrorNotificationService";
import List from "@mui/material/List";
import ErrorListEntry from "../ErrorNotifications/ErrorListEntry";
import React from "react";
import { ShipNotificationCompact, ShipsNotificationService } from "../../../../services/ShipsNotificationService";
import ShipNotification from "../../../../model/ShipNotification";
import ShipNotificationEntry from "./ShipsNotificationEntry";

interface NotificationListProps {
  pageChanger: (currentPage: CurrentPage) => void;
}

/**
 * This component is the second column of the main view of the application.
 * It displays the (software) errors (or warnings, info) that were caught in our
 * code and passed to ErrorNotificationService.
 *
 * @param pageChanger function that, when called, changes the page displayed in the second column.
 */
function NotificationList({ pageChanger }: NotificationListProps) {
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
        {getNotificationEntries()}
      </List>
    </Stack>
  );
}

/**
 * Gets all error notifications, reverses them (so that the newest one is the first
 * one), and returns a list of corresponding ErrorListEntry.
 */
function getNotificationEntries() {
  return ShipsNotificationService.getAllNotifications()
    .slice()
    .reverse()
    .map((notification: ShipNotificationCompact) => (
      // eslint-disable-next-line react/jsx-key
      <ShipNotificationEntry notification={notification} />
    ));
}

export default NotificationList;