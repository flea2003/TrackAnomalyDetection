import ShipDetails from "../model/ShipDetails";

class ShipUpdateBuffer {
  private static bufferedShips: Map<number, ShipDetails> = new Map();

  static resetBuffer() {
    this.bufferedShips = new Map();
  }

  static addToBuffer(newShip: ShipDetails) {
    this.bufferedShips.set(newShip.id, newShip);
  }

  static getBufferedShipsAndReset() {
    const returnValue = this.bufferedShips;
    this.resetBuffer();
    return returnValue;
  }
}

export default ShipUpdateBuffer;