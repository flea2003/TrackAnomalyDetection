import ShipDetails from "../model/ShipDetails";

/**
 * The class which buffers ships, so that updating ships (on the map)
 * can be done in larger batches (rather than updating each individual
 * ship every time).
 *
 * Used by WebSocket client, and is polled frequently.
 */
class ShipUpdateBuffer {
  private static bufferedShips: Map<number, ShipDetails> = new Map();

  /**
   * Clears the buffered ships.
   */
  static resetBuffer() {
    this.bufferedShips = new Map();
  }

  /**
   * Adds a new ship to the buffer.
   * If there was already a ship with the same id, it is replaced by a given new one.
   *
   * @param newShip a ship to add to the buffer
   */
  static addToBuffer(newShip: ShipDetails) {
    this.bufferedShips.set(newShip.id, newShip);
  }

  /**
   * Returns the buffered ships and also clears the buffer.
   */
  static getBufferedShipsAndReset() {
    const returnValue = this.bufferedShips;
    console.log("in buffer: " + returnValue.size);
    this.resetBuffer();
    return returnValue;
  }
}

export default ShipUpdateBuffer;
