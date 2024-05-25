import ErrorNotificationService, { ErrorNotification } from "../../../../services/ErrorNotificationService";
import Stack from "@mui/material/Stack";
import trashIcon from "../../../../assets/icons/trash.svg";
import React from "react";
import { ShipNotificationCompact } from "../../../../services/ShipsNotificationService";

interface NotificationEntryProps {
  notification: ShipNotificationCompact;
}

/**
 * This component is a single entry in the Error Notifications List.
 * It displays the (software) error that occurred.
 * The object to render is passed as a prop.
 *
 * @param notification ErrorNotification object which will be shown in this entry.
 */
function ShipNotificationEntry({ notification }: NotificationEntryProps) {

  return (
    <Stack
      direction="row"
      className="error-list-entry"
      data-testid="error-list-entry"
      spacing={0}
     // onClick={() => ErrorNotificationService.markAsRead(notification.id)}
    >
      <span className="error-list-entry-text">
        <div>
          {notification.message}
          {notification.shipID}
          {notification.id}
        </div>
      </span>
    </Stack>
  );
}

export default ShipNotificationEntry;