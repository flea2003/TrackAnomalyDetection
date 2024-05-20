import React from "react";
import Stack from "@mui/material/Stack";

import "../../../../styles/common.css";
import "../../../../styles/errorListEntry.css";
import ErrorNotificationService, {
  ErrorNotification,
} from "../../../../services/ErrorNotificationService";
import trashIcon from "../../../../assets/icons/trash.svg";

interface ErrorListEntryProps {
  notification: ErrorNotification;
}

/**
 * This component is a single entry in the Error Notifications List.
 * It displays the (software) error that occurred.
 * The object to render is passed as a prop.
 *
 * @param notification ErrorNotification object which will be shown in this entry.
 */
function ErrorListEntry({ notification }: ErrorListEntryProps) {
  const readStatusClassName = notification.wasRead
    ? "error-list-entry-read"
    : "error-list-entry-not-read";

  return (
    <Stack
      direction="row"
      className="error-list-entry"
      data-testid="error-list-entry"
      spacing={0}
      onClick={() => ErrorNotificationService.markAsRead(notification.id)}
    >
      <span className="error-list-entry-icon-container">
        <img
          src={notification.getIcon()}
          className="error-list-entry-icon"
          alt={notification.severity}
        />
      </span>
      <span className="error-list-entry-text">
        <div className={readStatusClassName} data-testid={readStatusClassName}>
          {notification.message}
        </div>
      </span>
      <span className="error-list-entry-icon-container">
        <img
          src={trashIcon}
          className="error-list-entry-trash-icon"
          data-testid="error-list-entry-trash-icon"
          alt="Trash Icon"
          onClick={() =>
            ErrorNotificationService.removeNotification(notification.id)
          }
        />
      </span>
    </Stack>
  );
}

export default ErrorListEntry;
