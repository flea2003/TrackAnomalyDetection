import '@testing-library/jest-dom';
import ShipService from "../services/ShipService";

jest.mock("../utils/HttpSender", () => ({
    __esModule: true,
    default: jest.fn().mockImplementation(() => ({
        get: jest.fn((endpoint) => {
            if (endpoint === '/ships/ais') {
                return Promise.resolve([{
                    id: "1",
                    speed: 350.0,
                    long: 29.0,
                    lat: 47.0,
                    course: 1,
                    departurePort: "p1",
                    heading: 1,
                    timestamp: "t1"
                }]);
            }
            else if (endpoint === '/ships/anomaly') {
                return Promise.resolve([{
                    id: "1",
                    anomalyScore: 1
                }]);
            }
        }),
    })),
}));



    test("backend-fetching", async () => {
        const result = await ShipService.queryBackendForShipsArray();
   expect(result.length==1).toBe(true);
});