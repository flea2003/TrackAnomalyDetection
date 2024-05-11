// Create a class to get ship information from the server
import ShipDetails from "../model/ShipDetails";
import HttpSender from "../utils/HttpSender";
import AISSignal from "../model/AISSignal";
import AnomalyInformation from "../model/AnomalyInformation";

class ShipService {
    static httpSender: HttpSender = new HttpSender();

    /**
     * This method queries the backend for both the Ships Array and the Anomaly Scores Array.
     * The resulting array of type ShipDetails is the result of aggregating the retrieved arrays.
     * The method returns a promise that resolves to an array of ShipDetails.
     * @returns a promise that resolves to an array of ShipDetails.
     */
    static queryBackendForShipsArray : (() => Promise<ShipDetails[]>) = () => {
        // Backend API endpoints for retrieving (polling) the information about
        // the latest AIS signals received for each ship
        // and the latest computed Anomaly Scores for each ship
        let shipsAISEndpoint = '/ships/ais';
        let shipsAnomalyInfoEndpoint = '/ships/anomaly';

        let AISResults = ShipService.httpSender.get(shipsAISEndpoint)
            .then(response => {
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
                return aisResults;
            });

        let AnomalyInfoResults = ShipService.httpSender.get(shipsAnomalyInfoEndpoint)
            .then(response => {
                const anomalyInfoResults: AnomalyInformation[] = response.map((item: any) => {
                    return {
                        id: item.shipHash,
                        anomalyScore: item.score
                    }
                });
                return anomalyInfoResults;
            })

        // As the resulting list of type ShipDetails is the result of an aggregation,
        // we have to wait for both Promise objects to resolve to lists as we can not
        // aggregate unresolved Promise objects
        const result =  Promise.all([AISResults, AnomalyInfoResults])
            .then(([aisResults, anomalyInfoResults]: [AISSignal[], AnomalyInformation[]]) => {
                return aisResults.reduce((result: ShipDetails[], aisSignal: AISSignal) => {
                    // We match the items based on the ID (hash) of the ship
                    const matchingAnomalyInfo = anomalyInfoResults.find((item) => item["id"] === aisSignal["id"]);
                    if(matchingAnomalyInfo){
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
}

export default ShipService;