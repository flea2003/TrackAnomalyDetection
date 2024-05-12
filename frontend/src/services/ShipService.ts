// Create a class to get ship information from the server
import ShipDetails from "../model/ShipDetails";
import HttpSender from "../utils/HttpSender";
import AISSignal from "../dtos/AISSignal";
import AnomalyInformation from "../dtos/AnomalyInformation";

class ShipService {
    static httpSender: HttpSender = new HttpSender();

    // Backend API endpoints for retrieving (polling) the information about
    // the latest AIS signals received for each ship
    // and the latest computed Anomaly Scores for each ship
    static shipsAISEndpoint = '/ships/ais';
    static shipsAnomalyInfoEndpoint = '/ships/anomaly';

    /**
     * This method queries the backend for both the Ships Array and the Anomaly Scores Array.
     * The resulting array of type ShipDetails is the result of aggregating the retrieved arrays.
     * The method returns a promise that resolves to an array of ShipDetails.
     * @returns a promise that resolves to an array of ShipDetails.
     */
    static queryBackendForShipsArray : (() => Promise<ShipDetails[]>) = () => {

        // Fetch the latest AIS signals of all monitored ships
        let AISResults = ShipService.getAllAISResults();

        // Fetch the latest computed anomaly scores of all monitored ships
        let AnomalyInfoResults = ShipService.getAnomalyInfoResults();

        // As the resulting list of type ShipDetails is the result of an aggregation,
        // we have to wait for both Promise objects to resolve to lists as we can not
        // aggregate unresolved Promise objects
        const result =  Promise.all([AISResults, AnomalyInfoResults])
            .then(([aisResults, anomalyInfoResults]: [AISSignal[], AnomalyInformation[]]) => {
                    return aisResults.reduce((result: ShipDetails[], aisSignal: AISSignal) => {
                        // We match the AISSignal items based on the ID (hash) of the ship
                        const matchingAnomalyInfo = anomalyInfoResults.find((item) => item["id"] === aisSignal["id"]);
                        if(matchingAnomalyInfo){
                            // Compose a ShipDetails instance given the matching AISSignal and AnomalyInformation items
                            const shipDetailsItem = ShipService.createShipDetailsFromDTOs(aisSignal, matchingAnomalyInfo);
                            result.push(shipDetailsItem);
                        }
                        return result;
                    }, []);
                    }
                )
        return result;
    };

    /**
     * Helper function that leverages the static instance of HttpSender in order to query the backend server
     * @returns - array of the latest DTOs that encapsulate the last received AIS info of the ships
     */
    static getAllAISResults: (() => Promise<AISSignal[]>) = () => {
        return ShipService.httpSender.get(ShipService.shipsAISEndpoint)
            .then(response => {
                // TODO: Implementing proper error handling for the cases in which the retrieved array is empty
                if(Array.isArray(response) && response.length > 0){
                    const aisResults: AISSignal[] = response.map((item: any) => {
                        return {
                            id: item.shipHash,
                            speed: item.speed,
                            long: item.longitude,
                            lat: item.latitude,
                            course: item.course,
                            departurePort: item.departurePort,
                            heading: item.heading,
                            timestamp: item.timestamp
                        }
                    })
                    return aisResults;}
                else{
                    return [];
                }
            });
    }

    /**
     * Helper function that leverages the static instance of HttpSender in order to query the backend server
     * @returns - array of the latest DTOs that encapsulate the last anomaly info of the ships
     */
    static getAnomalyInfoResults: (() => Promise<AnomalyInformation[]>) = () => {
        return ShipService.httpSender.get(ShipService.shipsAnomalyInfoEndpoint)
            .then(response => {
                // TODO: Implementing proper error handling for the cases in which the retrieved array is empty
                if(Array.isArray(response) && response.length > 0){
                    const anomalyInfoResults: AnomalyInformation[] = response.map((item: any) => {
                        return {
                            id: item.shipHash,
                            anomalyScore: item.score
                        }
                    });
                    return anomalyInfoResults;}
                else{
                    return [];
                }
            });
    }

    /**
     * Helper function that takes 2 matching instances of AISSignal and AnomalyInformation
     * and creates a new instance of ShipDetails that incorporates the information from them.
     * @param aisSignal - the instance that encapsulates the AIS info of a ship
     * @param anomalyInfo - the instance that encapsulates the anomaly info of a ship
     */
    static createShipDetailsFromDTOs(aisSignal: AISSignal, anomalyInfo: AnomalyInformation): ShipDetails {
        // TODO: Modify the 'explanation' field of the ShipDetails instance
        return new ShipDetails(aisSignal.id, aisSignal.heading, aisSignal.lat, aisSignal.long,
            anomalyInfo.anomalyScore,"", aisSignal.departurePort, aisSignal.course,
            aisSignal.speed);
    }
}
export default ShipService;