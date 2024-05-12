import '@testing-library/jest-dom';
import ShipService from "../services/ShipService";

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