import '@testing-library/jest-dom';
import ShipService from "../services/ShipService";

// Mock the HttpSender static instance of the ShipService class
// such that its get() method returns 2 proper arrays
describe("Test successful execution", () => {
    jest.mock('../services/ShipService', () => ({
        __esModule: true,
        default: {
            // Necessary for retaining the original queryBackendForShipsArray method
            // when creating the mock
            ...jest.requireActual('../services/ShipService').default,
            httpSender: {
                get: jest.fn((endpoint) => {
                    if(endpoint === '/ships/ais') {
                        return Promise.resolve([{
                            id: "1",
                            speed: 350.0,
                            long: 29.0,
                            lat: 47.0,
                            course: 90,
                            departurePort: "p1",
                            heading: 1,
                            timestamp: "t1"
                        }]);
                    } else if (endpoint === '/ships/anomaly') {
                        return Promise.resolve([{
                            id: "1",
                            anomalyScore: 1
                        }]);
                    }
                })
            },
        }
    }))

// TEST: successful aggregation of the fetched arrays
    test("backend-fetching-valid", async () => {
        ShipService.queryBackendForShipsArray().
        then((result) => {expect(result.length==1).toBe(true)});
    });
});

// Mock the HttpSender static instance of the ShipService class
// such that its get() method returns 2 empty arrays
describe('Execution with empty arrays', () => {
    jest.mock('../services/ShipService', () => ({
        __esModule: true,
        default: {
            // Necessary for retaining the original queryBackendForShipsArray method
            // when creating the mock
            ...jest.requireActual('../services/ShipService').default,
            httpSender: {
                get: jest.fn((endpoint) => {
                    if(endpoint === '/ships/ais') {
                        return Promise.resolve([]);
                    } else if (endpoint === '/ships/anomaly') {
                        return Promise.resolve([]);
                    }
                })
            },
        }
    }))
// TEST: aggregation of empty arrays
    test("backend-fetching-valid", async () => {
        ShipService.queryBackendForShipsArray().
        then((result) => {expect(result.length==0).toBe(true)});
    });
})

// Mock the HttpSender static instance of the ShipService class
// such that its get() method returns a proper array containing the anomaly information of 1 ship
// and an empty array corresponding to the AIS signal of that ship
describe('Execution with empty AIS array', () => {
    jest.mock('../services/ShipService', () => ({
        __esModule: true,
        default: {
            // Necessary for retaining the original queryBackendForShipsArray method
            // when creating the mock
            ...jest.requireActual('../services/ShipService').default,
            httpSender: {
                get: jest.fn((endpoint) => {
                    if(endpoint === '/ships/ais') {
                        return Promise.resolve([]);
                    } else if (endpoint === '/ships/anomaly') {
                        return Promise.resolve([{
                            id: "1",
                            anomalyScore: 1
                        }]);
                    }
                })
            },
        }
    }))

// TEST: aggregation with empty AIS array
    test("backend-fetching-valid", async () => {
        ShipService.queryBackendForShipsArray().
        then((result) => {expect(result.length==0).toBe(true)});
    });
})


// Mock the HttpSender static instance of the ShipService class
// such that its get() method returns two proper arrays with AIS and anomaly information
// for 2 different ships
describe('Execution with un-matching arrays', () => {
    jest.mock('../services/ShipService', () => ({
        __esModule: true,
        default: {
            // Necessary for retaining the original queryBackendForShipsArray method
            // when creating the mock
            ...jest.requireActual('../services/ShipService').default,
            httpSender: {
                get: jest.fn((endpoint) => {
                    if(endpoint === '/ships/ais') {
                        return Promise.resolve([{
                            id: "1",
                            speed: 350.0,
                            long: 29.0,
                            lat: 47.0,
                            course: 90,
                            departurePort: "p1",
                            heading: 1,
                            timestamp: "t1"
                        }]);
                    } else if (endpoint === '/ships/anomaly') {
                        return Promise.resolve([{
                            id: "2",
                            anomalyScore: 1
                        }]);
                    }
                })
            },
        }
    }))

// TEST: successful aggregation of the fetched arrays with un-matching ships
    test("backend-fetching-valid", async () => {
        ShipService.queryBackendForShipsArray().
        then((result) => {expect(result.length==0).toBe(true)});
    });
});


// Mock the HttpSender static instance of the ShipService class
// such that its get() method returns a proper array containing the AIS information of 1 ship
// and an empty array corresponding to the anomaly information of that ship
describe('Execution with empty AnomalyInfo array', () => {
    jest.mock('../services/ShipService', () => ({
        __esModule: true,
        default: {
            // Necessary for retaining the original queryBackendForShipsArray method
            // when creating the mock
            ...jest.requireActual('../services/ShipService').default,
            httpSender: {
                get: jest.fn((endpoint) => {
                    if(endpoint === '/ships/ais') {
                        return Promise.resolve([{
                            id: "1",
                            speed: 350.0,
                            long: 29.0,
                            lat: 47.0,
                            course: 90,
                            departurePort: "p1",
                            heading: 1,
                            timestamp: "t1"
                        }]);
                    } else if (endpoint === '/ships/anomaly') {
                        return Promise.resolve([]);
                    }
                })
            },
        }
    }))

// TEST: successful aggregation of the fetched arrays with un-matching ships
    test("backend-fetching-valid", async () => {
        ShipService.queryBackendForShipsArray().
        then((result) => {expect(result.length==0).toBe(true)});
    });
});