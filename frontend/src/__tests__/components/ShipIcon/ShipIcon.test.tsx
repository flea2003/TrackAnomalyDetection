import { createShipIcon } from "../../../components/ShipIcon/ShipIcon";
// import { setupJestCanvasMock } from "jest-canvas-mock";

// beforeEach(() => {
//   jest.clearAllMocks();
//   setupJestCanvasMock();
// });

test('arrow is drawn when ship is moving', () => {
  createShipIcon(0.5, 90, true);
})