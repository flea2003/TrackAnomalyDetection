import React from "react";
import { useState } from "react";
import closeIcon from "../../../../assets/icons/helper-icons/close.svg";
import filterIconBlue from "../../../../assets/icons/selected-sidebar-icons/filter-blue.png";
import filterIcon from "../../../../assets/icons/anomaly-list/filter.svg";

import Stack from "@mui/material/Stack";
import { CurrentPage } from "../../../../App";

import "../../../../styles/anomaly-list/anomalyList.css";
import "../../../../styles/anomaly-list/slider.css";

interface ObjectDetailsProps {
  pageChanger: (currentPage: CurrentPage) => void;
  setFilterThreshold: (value: number) => void;
  anomalyThreshold: number;
}

/**
 * Function that returns the visual component of the title of the anomaly list,
 * which can be extended to set the threshold of the anomaly score which is used
 * for filtering ships
 *
 * @param pageChanger page changer function
 * @param setFilterThreshold function that sets the anomaly threshold
 * @param anomalyThreshold the anomaly threshold that is used for filtering
 * @constructor
 */
const AnomalyTitleWithSlider = ({
  pageChanger,
  setFilterThreshold,
  anomalyThreshold,
}: ObjectDetailsProps) => {
  // State to manage the visibility of the extended container
  const [isExtended, setIsExtended] = useState(false);

  // Function used to alter the state of whether the slider is shown or not
  const toggleExtended = () => {
    setIsExtended((prevState) => !prevState);
  };

  // Function to handle the change in slider
  const handleSliderChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const value = Number(event.target.value);
    setFilterThreshold(value);
  };

  return (
    <Stack id="anomaly-list-title-container-with-slider" direction={"column"}>
      <Stack id="anomaly-list-title-container" direction="row">
        <img
          src={closeIcon}
          alt="Close"
          id="anomaly-list-close-icon"
          data-testid="anomaly-list-close-icon"
          onClick={() => pageChanger({ currentPage: "none", shownItemId: -1 })}
        />
        <div className="list-title">Anomaly List</div>
        <div className="modify-button-container" onClick={toggleExtended}>
          {!isExtended && (
            <img src={filterIcon} alt="Open" className="filter-icon" />
          )}

          {isExtended && (
            <img
              src={filterIconBlue}
              alt="Open"
              data-testid="anomaly-list-close-icon"
              className="filter-icon"
            />
          )}
        </div>
      </Stack>
      {isExtended && (
        <span className="modify-threshold-slide-container">
          <input
            type="range"
            min="0"
            max="100"
            value={anomalyThreshold}
            onChange={handleSliderChange}
          />
          <div className="threshold-div">{anomalyThreshold}%</div>
        </span>
      )}
    </Stack>
  );
};

export default AnomalyTitleWithSlider;
