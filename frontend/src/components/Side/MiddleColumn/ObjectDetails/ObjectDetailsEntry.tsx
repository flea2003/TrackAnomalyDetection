import React from "react";

import "../../../../styles/common.css";
import "../../../../styles/objectDetailsEntry.css";

/**
 * This component is a single entry in the Object Details. It displays a single key-value pair of the object.
 * In reality, it simply displays type on one line and value on the other with some styling.
 * The property type and value are passed as props.
 */
function ObjectDetailsEntry(props: { type: string; value: string }) {
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

export default ObjectDetailsEntry;
