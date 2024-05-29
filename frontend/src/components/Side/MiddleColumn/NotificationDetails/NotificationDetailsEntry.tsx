import React from "react";

import "../../../../styles/common.css";
import "../../../../styles/objectDetailsEntry.css";
import ObjectDetailsEntry from "../ObjectDetails/ObjectDetailsEntry";

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
      <br />
      <span className="object-details-entry-value">
        {props.type === "Explanation"
          ? getExplanationList(props.value)
          : props.value}
      </span>
    </div>
  );
}

/**
 * FOR NOW I JUST COPIED THIS FROM ALDAS IMPLEMENTATION.
 * later I think we should refactor in a way that reused components could be
 * accessible (so have a common components function)
 *
 * @param str
 */
function getExplanationList(str: string) {
  str = str.trim();

  if (str === "") {
    return "No anomalous behaviour registered.";
  }

  return (
    <ul className="object-details-entry-value">
      {str.split("\n").map((line, index) => (
        <li key={index}>{line}</li>
      ))}
    </ul>
  );
}

export default NotificationDetailsEntry;
