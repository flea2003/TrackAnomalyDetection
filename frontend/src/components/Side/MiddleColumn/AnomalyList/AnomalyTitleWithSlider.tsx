import React from "react";
import { useState } from "react";
import "../../../../styles/anomalyList.css";
import closeIcon from "../../../../assets/icons/close.svg";
import extendIcon from "../../../../assets/icons/extend_info.svg";
import Stack from "@mui/material/Stack";
import { CurrentPage } from "../../../../App";
import ShipService from "../../../../services/ShipService";

interface ObjectDetailsProps {
  pageChanger: (currentPage: CurrentPage) => void;
}

const AnomalyTitleWithSlider = (props: ObjectDetailsProps) => {
  // State to manage the visibility of the extended container
  const [isExtended, setIsExtended] = useState(false);
  // State to manage the slider value
  const [threshold, setThreshold] = useState(ShipService.filterThreshold);

  // Function used to alter the state of whether the slider is shown or not
  const toggleExtended = () => {
    setIsExtended((prevState) => !prevState);
  };

  // Function to handle the change in slider
  const handleSliderChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setThreshold(Number(event.target.value));
    ShipService.filterThreshold = Number(event.target.value);
  };

  return (
    <Stack id="anomaly-list-title-container-with-slider" direction={"column"}>
      <Stack id="anomaly-list-title-container" direction="row">
        <img
          src={closeIcon}
          alt="Close"
          id="anomaly-list-close-icon"
          data-testid="anomaly-list-close-icon"
          onClick={() =>
            props.pageChanger({ currentPage: "none", shownItemId: -1 })
          }
        />
        <div className="modify-button-container" onClick={toggleExtended}>
          Threshold
          <img src={extendIcon} alt="close" />
        </div>
      </Stack>
      {isExtended && (
        <span className="modify-threshold-slide-container">
          <input
            type="range"
            min="0"
            max="100"
            className="modify-threshold-slide"
            value={threshold}
            onChange={handleSliderChange}
          />
          {threshold}%
        </span>
      )}
    </Stack>
  );
};

export default AnomalyTitleWithSlider;
