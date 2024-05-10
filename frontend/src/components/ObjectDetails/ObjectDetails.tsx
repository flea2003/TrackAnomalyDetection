import React from "react";
import Stack from "@mui/material/Stack";
import List from "@mui/material/List";
import ShipDetails from "../../model/ShipDetails";
import ObjectDetailsEntry from "./ObjectDetailsEntry";

import "../../styles/common.css";
import "../../styles/objectDetails.css";

interface ObjectDetailsProps {
    ships: ShipDetails[],
    shipId: string,
    pageChanger: Function
}

/**
 * This component is the second column of the main view of the application. It displays the details of a selected object.
 * The object to whose details are to be displayed is passed as a prop.
 *
 * @param props properties passed to this component. Most importantly, it contains the ship object whose details to display.
 */
function ObjectDetails(props: ObjectDetailsProps) {

    // Extract the props
    const allShips = props.ships;
    const shipID = props.shipId;
    const pageChanger = props.pageChanger;

    // Find the ship with the given ID in the map. If such ship is not (longer) present, show a message.
    const ship = allShips.find((ship) => ship.id === shipID);
    if (ship === undefined) {
        return (
            <Stack id="object-details-container">
                <span className="object-details-title">Object ID:&nbsp; <span className="object-details-title-id">Not found</span></span>
            </Stack>
        )
    }

    // Create a list of properties to display
    const properties = ship.getPropertyList();
    const propertyList = properties.map((property) => {
        return <ObjectDetailsEntry type={property.type} value={property.value} />
    });

    // Define the return icon and its click handler
    const returnIcon = require("../../assets/icons/back.svg").default;
    const onReturnClicked = () => {
        pageChanger({currentPage: "anomalyList", shownShipId: ""});
    }

    return (
        <Stack id="object-details-container">
            <div className="object-details-title-container">
                <img src={returnIcon} className="object-details-return-icon" onClick={onReturnClicked}/>
                <span className="object-details-title">Object ID:&nbsp; <span className="object-details-title-id">{ship.id}</span> </span>
            </div>
            <List style={{maxHeight: '100%', overflow: 'auto'}} className="object-details-list">
                {propertyList}
            </List>
        </Stack>
    )
}

export default ObjectDetails;