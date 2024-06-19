import ShipDetails from "./ShipDetails";

class ShipNotification {
  id: number;
  isRead: boolean;
  shipDetails: ShipDetails;

  /**
   * Constructor for the notifications class
   *
   * @param id id of the notification
   * @param isRead status of whether the notification was read
   * @param shipDetails corresponding ship details of the notification
   */
  constructor(id: number, isRead: boolean, shipDetails: ShipDetails) {
    this.id = id;
    this.isRead = isRead;
    this.shipDetails = shipDetails;
  }
}

export default ShipNotification;
