class ShipDetails{
    id: string;
    heading: number;
    lat: number;
    lng: number;
    anomalyScore: number;
    explanation: string;
    departurePort: string;
    course: number;
    speed: number;

    constructor(id: string, heading: number, lat: number, lng: number, anomalyScore: number, explanation: string,
                departurePort: string, course: number, speed: number) {
        this.id = id;
        this.heading = heading;
        this.lat = lat;
        this.lng = lng;
        this.anomalyScore = anomalyScore;
        this.explanation = explanation;
        this.departurePort = departurePort;
        this.course = course;
        this.speed = speed;

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
            {type: "Longitude", value: "" + this.lng},
            {type: "Latitude", value: "" + this.lat},
            {type: "Heading", value: "" + this.heading},
            {type: "Departure Port", value: "" + this.departurePort},
            {type: "Course", value: "" + this.course},
            {type: "Speed", value: "" + this.speed}
        ];
    }
}

export default ShipDetails;