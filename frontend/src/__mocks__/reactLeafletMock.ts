jest.genMockFromModule('react-leaflet');

// eslint-disable-next-line @typescript-eslint/no-empty-function
const MapContainer = () => {};
// eslint-disable-next-line @typescript-eslint/no-empty-function
const TileLayer = () => {};

module.exports = { MapContainer, TileLayer };