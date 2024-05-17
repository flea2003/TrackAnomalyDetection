import React from "react";
import Stack from "@mui/material/Stack";

import "../../styles/common.css";
import "../../styles/sidebar.css";

import shipIcon from "../../assets/icons/ship.png";
import bellIcon from "../../assets/icons/bell-notification.svg";
import settingsIcon from "../../assets/icons/settings.svg";
import { CurrentPage } from "../../App";

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

  // Define the click handlers for the icons
  const onShipIconClicked = () =>
    pageChanger({ currentPage: "anomalyList", shownShipId: -1 });
  const onBellIconClicked = () =>
    pageChanger({ currentPage: "notifications", shownShipId: -1 });
  const onSettingsIconClicked = () =>
    pageChanger({ currentPage: "settings", shownShipId: -1 });

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
        className="sidebar-entry"
        onClick={onBellIconClicked}
      >
        <img src={bellIcon} className="sidebar-icon" alt={bellIconAlt} />
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
    </Stack>
  );
}

export default Sidebar;
