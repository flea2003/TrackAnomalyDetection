import React, { useState } from "react";
import Stack from "@mui/material/Stack";
import { CurrentPage } from "../../../App";
import ErrorNotificationService from "../../../services/ErrorNotificationService";
import shipIcon from "../../../assets/icons/anomaly-list/ship.png";
import bellIconNotRead from "../../../assets/icons/regular-notifications/notificaation_bell_orange.png";
import bellIconRead from "../../../assets/icons/regular-notifications/notification_bell.svg";
import settingsIcon from "../../../assets/icons/helper-icons/settings.svg";
import bugIcon from "../../../assets/icons/error-notifications/bug.svg";
import bugIconRed from "../../../assets/icons/error-notifications/bug-red.png";

import shipIconSelected from "../../../assets/icons/selected-sidebar-icons/ship-blue.png";
import notificationIconSelected from "../../../assets/icons/selected-sidebar-icons/notification-bell-blue.png";
import settingsIconSelected from "../../../assets/icons/selected-sidebar-icons/settings-blue.png";
import bugIconSelected from "../../../assets/icons/selected-sidebar-icons/bug_blue.png";

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

  const [displayedAnomalyList, setDisplayedAnomalyList] = useState(false);
  const [displayedNotifications, setDisplayedNotifications] = useState(false);
  const [displayedSettings, setDisplayedSettings] = useState(false);
  const [displayedBugs, setDisplayedBugs] = useState(false);

  const changeAnomalyListIcon = () => {
    setDisplayedAnomalyList((x) => !x);
    setDisplayedNotifications(false);
    setDisplayedSettings(false);
    setDisplayedBugs(false);
  };

  const changeNotificationsIcon = () => {
    setDisplayedAnomalyList(false);
    setDisplayedNotifications((x) => !x);
    setDisplayedSettings(false);
    setDisplayedBugs(false);
  };

  const changeSettingsIcon = () => {
    setDisplayedAnomalyList(false);
    setDisplayedNotifications(false);
    setDisplayedSettings((x) => !x);
    setDisplayedBugs(false);
  };

  const changeBugsIcon = () => {
    setDisplayedAnomalyList(false);
    setDisplayedNotifications(false);
    setDisplayedSettings(false);
    setDisplayedBugs((x) => !x);
  };

  // Define the click handlers for the icons
  const onShipIconClicked = () => {
    pageChanger({ currentPage: "anomalyList", shownItemId: -1 });
    changeAnomalyListIcon();
  };

  const onBellIconClicked = () => {
    pageChanger({ currentPage: "notificationList", shownItemId: -1 });
    changeNotificationsIcon();
  };

  const onSettingsIconClicked = () => {
    pageChanger({ currentPage: "settings", shownItemId: -1 });
    changeSettingsIcon();
  };

  const onBugIconClicked = () => {
    pageChanger({ currentPage: "errors", shownItemId: -1 });
    changeBugsIcon();
  };

  return (
    <Stack id="sidebar" data-testid="sidebar">
      <span
        data-testid="sidebar-ship-icon"
        className="sidebar-entry"
        onClick={onShipIconClicked}
      >
        {!displayedAnomalyList && (
          <img
            src={shipIcon}
            className="anomaly-list-icon-not-selected"
            alt={shipIconAlt}
          />
        )}
        {displayedAnomalyList && (
          <img
            src={shipIconSelected}
            className="anomaly-list-icon-selected"
            alt={shipIconAlt}
          />
        )}
      </span>
      <span
        data-testid="sidebar-bell-icon"
        className="sidebar-bell-icon"
        onClick={onBellIconClicked}
      >
        {!displayedNotifications && (
          <img
            src={getNotificationsBellType().toString()}
            className="bell-icon-not-selected"
            alt={bellIconAlt}
          />
        )}
        {displayedNotifications && (
          <img
            src={notificationIconSelected}
            className="bell-icon-selected"
            alt={bellIconAlt}
          />
        )}
      </span>
      <span
        data-testid="sidebar-settings-icon"
        className="sidebar-entry"
        onClick={onSettingsIconClicked}
      >
        {!displayedSettings && (
          <img
            src={settingsIcon}
            className="settings-icon-not-selected"
            alt={settingsIconAlt}
          />
        )}
        {displayedSettings && (
          <img
            src={settingsIconSelected}
            className="settings-icon-selected"
            alt={settingsIconAlt}
          />
        )}
      </span>
      <span
        data-testid="sidebar-bug-icon"
        className="sidebar-entry"
        onClick={onBugIconClicked}
      >
        {!displayedBugs && (
          <img
            src={getBugIcon()}
            className="bug-icon-not-selected"
            alt={bugIconAlt}
          />
        )}
        {displayedBugs && (
          <img
            src={bugIconSelected}
            className="bug-icon-selected"
            alt={bugIconAlt}
          />
        )}
      </span>
    </Stack>
  );
}

function getBugIcon() {
  if (ErrorNotificationService.areAllRead()) {
    return bugIcon;
  }
  return bugIconRed;
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
