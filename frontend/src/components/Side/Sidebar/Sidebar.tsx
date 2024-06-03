import React from "react";
import Stack from "@mui/material/Stack";
import { CurrentPage } from "../../../App";
import ErrorNotificationService from "../../../services/ErrorNotificationService";
import shipIcon from "../../../assets/icons/ship.png";
import bellIconNotRead from "../../../assets/icons/regular-notifications/bell-notification-not-read.svg";
import bellIconRead from "../../../assets/icons/regular-notifications/bell-notification-read.svg";
import settingsIcon from "../../../assets/icons/settings.svg";
import bugIcon from "../../../assets/icons/bug.svg";
import { NotificationService } from "../../../services/NotificationService";

import "../../../styles/common.css";
import "../../../styles/sidebar.css";

interface SidebarProps {
  pageChanger: (currentPage: CurrentPage) => void;
}

/**
 * This prop is the sidebar of the application (third column). It contains three icons that are placeholders
 * for future functionality.
 *
 * @param pageChanger function that, when called, changes the page displayed in the second column.
 */
function Sidebar({ pageChanger }: SidebarProps) {
  // Load the icons

  const shipIconAlt = "Ship Icon";
  const bellIconAlt = "Bell Icon";
  const settingsIconAlt = "Settings Icon";
  const bugIconAlt = "Bug Icon";

  // Define the click handlers for the icons
  const onShipIconClicked = () =>
    pageChanger({ currentPage: "anomalyList", shownItemId: -1 });
  const onBellIconClicked = () =>
    pageChanger({ currentPage: "notificationList", shownItemId: -1 });
  const onSettingsIconClicked = () =>
    pageChanger({ currentPage: "settings", shownItemId: -1 });
  const onBugIconClicked = () =>
    pageChanger({ currentPage: "errors", shownItemId: -1 });

  return (
    <Stack id="sidebar" data-testid="sidebar">
      <span
        data-testid="sidebar-ship-icon"
        className="sidebar-entry"
        onClick={onShipIconClicked}
      >
        <img src={shipIcon} className="sidebar-icon" alt={shipIconAlt} />
      </span>
      <span
        data-testid="sidebar-bell-icon"
        className="sidebar-bell-icon"
        onClick={onBellIconClicked}
      >
        <img
          src={getNotificationsBellType().toString()}
          className="sidebar-icon"
          alt={bellIconAlt}
        />
      </span>
      <span
        data-testid="sidebar-settings-icon"
        className="sidebar-entry"
        onClick={onSettingsIconClicked}
      >
        <img
          src={settingsIcon}
          className="sidebar-icon"
          alt={settingsIconAlt}
        />
      </span>
      <span
        data-testid="sidebar-bug-icon"
        className={getBugIconClassName()}
        onClick={onBugIconClicked}
      >
        <img src={bugIcon} className="sidebar-icon" alt={bugIconAlt} />
      </span>
    </Stack>
  );
}

/**
 * Returns the class name for the icon element that represents error list
 * in the sidebar.
 * The class name is based on whether all notifications were read or not,
 * so that the icon could be shown in red when there are unread notifications.
 */
function getBugIconClassName() {
  if (ErrorNotificationService.areAllRead()) {
    return "sidebar-bug-icon-all-read";
  }
  return "sidebar-bug-icon-not-all-read";
}

/**
 * Changes the style of the notification bell background based on whether
 * there are any unread notifications
 */
function getNotificationsBellType() {
  if (NotificationService.areAllRead()) {
    return bellIconRead;
  }
  return bellIconNotRead;
}

export default Sidebar;
