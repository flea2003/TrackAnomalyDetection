import React, { useState } from "react";
import Stack from "@mui/material/Stack";
import { CurrentPage } from "../../../App";
import ErrorNotificationService from "../../../services/ErrorNotificationService";
import shipIcon from "../../../assets/icons/anomaly-list/ship.png";
import bellIconNotRead from "../../../assets/icons/regular-notifications/notification_bell_orange.png";
import bellIconRead from "../../../assets/icons/regular-notifications/notification_bell.svg";
import bugIcon from "../../../assets/icons/error-notifications/bug.svg";
import bugIconRed from "../../../assets/icons/error-notifications/bug-red.png";
import shipIconSelected from "../../../assets/icons/selected-sidebar-icons/ship-blue.png";
import notificationIconSelected from "../../../assets/icons/selected-sidebar-icons/notification-bell-blue.png";
import bugIconSelected from "../../../assets/icons/selected-sidebar-icons/bug_blue.png";
import ShipNotification from "../../../model/ShipNotification";
import { NotificationService } from "../../../services/NotificationService";

import "../../../styles/common.css";
import "../../../styles/sidebar.css";

interface SidebarProps {
  pageChanger: (currentPage: CurrentPage) => void;
  currentPage: CurrentPage;
  notifications: ShipNotification[];
}

/**
 * This prop is the sidebar of the application (third column). It contains three icons that are placeholders
 * for future functionality.
 *
 * @param pageChanger function that, when called, changes the page displayed in the second column.
 * @param currentPage current page that is being displayed
 * @param notifications all notifications stored in frontend
 */
function Sidebar({ pageChanger, currentPage, notifications }: SidebarProps) {
  // Load the icons

  const shipIconAlt = "Ship Icon";
  const bellIconAlt = "Bell Icon";
  const bugIconAlt = "Bug Icon";

  const [displayedAnomalyList, setDisplayedAnomalyList] = useState(false);
  const [displayedNotifications, setDisplayedNotifications] = useState(false);
  const [displayedBugs, setDisplayedBugs] = useState(false);

  const changeAnomalyListIcon = () => {
    setDisplayedAnomalyList((x) => !x);
    setDisplayedNotifications(false);
    setDisplayedBugs(false);
  };

  const changeNotificationsIcon = () => {
    setDisplayedAnomalyList(false);
    setDisplayedNotifications((x) => !x);
    setDisplayedBugs(false);
  };

  const changeBugsIcon = () => {
    setDisplayedAnomalyList(false);
    setDisplayedNotifications(false);
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

  const onBugIconClicked = () => {
    pageChanger({ currentPage: "errors", shownItemId: -1 });
    changeBugsIcon();
  };

  if (currentPage.currentPage === "none") {
    if (displayedBugs || displayedNotifications || displayedAnomalyList) {
      setDisplayedAnomalyList(false);
      setDisplayedNotifications(false);
      setDisplayedBugs(false);
    }
  }

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
            src={getNotificationsBell(notifications).toString()}
            className={getNotificationsClass(notifications)}
            alt={bellIconAlt}
          />
        )}
        {displayedNotifications && (
          <img
            src={
              NotificationService.areAllRead(notifications)
                ? notificationIconSelected
                : bellIconNotRead
            }
            className="bell-icon-selected"
            alt={bellIconAlt}
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
            src={getBugIconNotSelected()}
            className={getErrorNotificationsClass()}
            alt={bugIconAlt}
          />
        )}
        {displayedBugs && (
          <img
            src={
              ErrorNotificationService.areAllRead()
                ? bugIconSelected
                : bugIconRed
            }
            className="bug-icon-selected"
            alt={bugIconAlt}
          />
        )}
      </span>
    </Stack>
  );
}

function getBugIconNotSelected() {
  if (ErrorNotificationService.areAllRead()) {
    return bugIcon;
  }
  return bugIconRed;
}

/**
 * Changes the icon of the notification bell based on whether
 * there are any unread notifications
 *
 * @param notifications all notifications stored in frontend
 */
function getNotificationsBell(notifications: ShipNotification[]) {
  if (NotificationService.areAllRead(notifications)) {
    return bellIconRead;
  }
  return bellIconNotRead;
}

/**
 * Changes the style of the notification bell when notifications window is not displayed,
 * based on whether all notifications are read or not (the hovering should differ
 * for these scenarios)
 *
 * @param notifications all notifications stored in frontend
 */
function getNotificationsClass(notifications: ShipNotification[]) {
  if (NotificationService.areAllRead(notifications)) {
    return "bell-icon-not-selected-all-read";
  }
  return "bell-icon-not-selected-not-all-read";
}

/**
 * Changes the style of the bug notifications icon when notifications window is not displayed,
 * based on whether all error notifications are read or not (the hovering should differ
 * for these scenarios)
 */
function getErrorNotificationsClass() {
  if (ErrorNotificationService.areAllRead()) {
    return "bug-icon-not-selected-all-read";
  }
  return "bug-icon-not-selected-not-all-read";
}

export default Sidebar;
