import React from "react";
import Stack from "@mui/material/Stack";
import List from "@mui/material/List";
import ShipDetails from "../../model/ShipDetails";
import ObjectDetailsEntry from "./ObjectDetailsEntry";

import "../../styles/common.css";
import "../../styles/objectDetails.css";

/**
 * This component is the second column of the main view of the application. It displays the details of a selected object.
 * The object to whose details are to be displayed is passed as a prop.
 */
function ObjectDetails(props: { ship: ShipDetails}): JSX.Element {

    const ship = props.ship;

    const entries = [];

    // Just an example. In fact, these details should be extracted from the ship object.
    entries.push(<ObjectDetailsEntry type={"Object type"} value={"Ship"} />);
    entries.push(<ObjectDetailsEntry type={"Anomaly score"} value={"95%"} />);
    entries.push(<ObjectDetailsEntry type={"Explanation"} value={"The ship has been travelling faster than 30 knots for more than 15 minutes."} />);
    entries.push(<ObjectDetailsEntry type={"Speed"} value={"10 knots"} />);
    entries.push(<ObjectDetailsEntry type={"Longitude"} value={"" + ship.lng} />);
    entries.push(<ObjectDetailsEntry type={"Latitude"} value={"" + ship.lat} />);
    entries.push(<ObjectDetailsEntry type={"Heading"} value={"" + ship.heading} />);

    const returnIcon = require("../../assets/icons/back.svg").default;

    return (
        <Stack id="object-details-container">
            <div className="object-details-title-container">
                <img src={returnIcon} className="object-details-return-icon" />
                <span className="object-details-title">Object ID:&nbsp; <span className="object-details-title-id">{ship.name}</span> </span>
            </div>
            <List style={{maxHeight: '100%', overflow: 'auto'}} className="object-details-list">
                {entries}
            </List>
        </Stack>
    )
}

export default ObjectDetails;