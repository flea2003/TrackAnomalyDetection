import React from "react";

import "../../../../styles/common.css";
import "../../../../styles/objectDetailsEntry.css";

/**
 * This component is a single entry in the Notification details. It displays a
 * single key-value pair of the object. In reality, it simply displays type on
 * one line and value on the other with some styling. The property type and
 * value are passed as props.
 */
function NotificationDetailsEntry(props: { type: string; value: string }) {
  return (
    <div className="object-details-entry">
      <span className="object-details-entry-type">{props.type}</span>
      <br/>
      <span className="object-details-entry-value">{props.value}</span>
    </div>
  );
}

export default NotificationDetailsEntry;
