class ShipDetails{
    static rounding: number = 1000;
    id: string;
    heading: number;
    lat: number;
    lng: number;
    anomalyScore: number;
    explanation: string;

    constructor(id: string, heading: number, lat: number, lng: number, anomalyScore: number, explanation: string) {
        this.id = id;
        this.heading = heading;
        this.lat = lat;
        this.lng = lng;
        this.anomalyScore = anomalyScore;
        this.explanation = explanation;
    }

    /**
     * This method returns a list of properties of the ship. This is used to present the properties in a human-readable format,
     * when the ship details page is opened. This should not include the name of the ship.
     *
     * @returns a list of properties of the ship
     */
    getPropertyList() {
        return [
            {type: "Object type", value: "Ship"},
            {type: "Anomaly score", value: this.anomalyScore + "%"},
            {type: "Explanation", value: this.explanation},
            {type: "Latitude", value: "" +  (Math.round(this.lat * ShipDetails.rounding) / ShipDetails.rounding) },
            {type: "Longitude", value: "" + (Math.round(this.lng * ShipDetails.rounding) / ShipDetails.rounding) },
            {type: "Heading", value: "" + this.heading}
        ];
    }
}

export default ShipDetails;