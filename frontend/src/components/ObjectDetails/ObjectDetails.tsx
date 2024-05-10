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
 *
 * @param props properties passed to this component. Most importantly, it contains the ship object whose details to display.
 */
function ObjectDetails(props: { ship: ShipDetails}) {

    const ship = props.ship;

    const properties = ship.getPropertyList();
    const propertyList = properties.map((property) => {
        return <ObjectDetailsEntry type={property.type} value={property.value} />
    });

    const returnIcon = require("../../assets/icons/back.svg").default;

    return (
        <Stack id="object-details-container">
            <div className="object-details-title-container">
                <img src={returnIcon} className="object-details-return-icon" />
                <span className="object-details-title">Object ID:&nbsp; <span className="object-details-title-id">{ship.name}</span> </span>
            </div>
            <List style={{maxHeight: '100%', overflow: 'auto'}} className="object-details-list">
                {propertyList}
            </List>
        </Stack>
    )
}

export default ObjectDetails;