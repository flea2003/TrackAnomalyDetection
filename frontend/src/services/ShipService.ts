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
                    // We match the items based on the ID (hash) of the ship
                    const matchingAnomalyInfo = anomalyInfoResults.find((item) => item["id"] === aisSignal["id"]);
                    if(matchingAnomalyInfo){
                        // TODO: Modify the 'explanation' field of the ShipDetails instance
                        const shipDetailsItem = new ShipDetails(aisSignal.id,aisSignal.heading,aisSignal.lat, aisSignal.long,
                            matchingAnomalyInfo.anomalyScore,"",aisSignal.departurePort,aisSignal.course,
                            aisSignal.speed);
                        result.push(shipDetailsItem);
                    }
                    return result;
                }, []);
                }
            )
        return result;
    };

    static getAllAISResults: (() => Promise<AISSignal[]>) = () => {
        return ShipService.httpSender.get(ShipService.shipsAISEndpoint)
            .then(response => {
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

    static getAnomalyInfoResults: (() => Promise<AnomalyInformation[]>) = () => {
        return ShipService.httpSender.get(ShipService.shipsAnomalyInfoEndpoint)
            .then(response => {
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

}

export default ShipService;