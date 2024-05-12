// Create a class to get ship information from the server
import ShipDetails from "../model/ShipDetails";
import HttpSender from "../utils/HttpSender";

class ShipService {
    private httpSender: HttpSender = new HttpSender();

    /**
     * This method queries the backend for the ships array. It returns a promise that resolves to an array of ShipDetails.
     * At the moment, the ships are hardcoded in, but this method can (and should) be modified to query the backend.
     *
     * When implementing, please make sure that the method returns Promist<ShipDetails[]> type.
     * If you implement correctly, you will see the map be updated every second with the details that are fetched from the server.
     * I.e., nothing has to be implemented in the Map or App. Only this method remains for full communication with the server to be finished.
     * Also, maybe ShipDetails.ts might have to be updated to deal with JSON parsing.
     *
     * @returns a promise that resolves to an array of ShipDetails.
     */
    static queryBackendForShipsArray : (() => Promise<ShipDetails[]>) = () => {
        var shipsArray = [
            new ShipDetails('1', 180, 10.695212883123546, 10.5444375499444,  12, 'The ship has been travelling faster than 30 knots for more than 15 minutes.'),
            new ShipDetails('2', 20, 20.695212883123546, 20.5444375499444,  52, 'The ship has been travelling faster than 30 knots for more than 15 minutes.'),
            new ShipDetails('3', 30, 30.695212883123546, 30.5444375499444,  52, 'The ship has been travelling faster than 30 knots for more than 15 minutes.'),
            new ShipDetails('4', 40, 40.695212883123546, 40.5444375499444,  52, 'The ship has been travelling faster than 30 knots for more than 15 minutes.'),
            new ShipDetails('5', 90, 90.695212883123546, 90.5444375499444,  52, 'The ship has been travelling faster than 30 knots for more than 15 minutes.'),
        ];
        shipsArray.forEach((ship: ShipDetails) => {
            ship.anomalyScore += (Math.floor(Math.random() * 10) - 2);
            ship.lat += Math.random() * 5;
            ship.heading += 10;
        });
        return new Promise((resolve) => {
             resolve(shipsArray);
        });
    };
}

export default ShipService;