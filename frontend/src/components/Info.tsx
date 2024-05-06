import React from "react";

function Info(props: { ship: ShipDetails }): JSX.Element {
    const { ship } = props;

    return (
        <div>
            <h2>{ship.name}</h2>
            <p><strong>Color:</strong> {ship.color}</p>
            <p><strong>Heading:</strong> {ship.heading}</p>
            <p><strong>Coordinates:</strong> {ship.lat}, {ship.lng}</p>
        </div>
    );
}

export default Info;