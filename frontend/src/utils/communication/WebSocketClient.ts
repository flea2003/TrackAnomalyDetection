import { useEffect, useState } from "react";
import ShipDetails from "../../model/ShipDetails";
import { Client } from "@stomp/stompjs";
import APIResponseItem from "../../templates/APIResponseItem";
import ShipService from "../../services/ShipService";
import ErrorNotificationService from "../../services/ErrorNotificationService";
import websocketConfig from "../../configs/websocketConfig.json";
import ShipUpdateBuffer from "../../services/ShipUpdateBuffer";

const useWebSocketClient = () => {
  const [ships, setShips] = useState<Map<number, ShipDetails>>(new Map());

  // Update the ships based on buffer
  useEffect(() => {
    const intervalId = setInterval(() => {
      const newShips = new Map(ships);
      ShipUpdateBuffer.getBufferedShipsAndReset().forEach(shipToUpdate => {
        newShips.set(shipToUpdate.id, shipToUpdate);
      });
      console.log("updating ships basedd on buffer")
      setShips(newShips);
    }, 10000);

    return () => {
      clearInterval(intervalId);
    }
  }, [ships]);

  /**
   * Configure the WebSocket connection.
   */
  useEffect(() => {
    const stompClient = new Client({
      brokerURL: websocketConfig.brokerUrl,

      // Try to reconnect to the backend broker after 60 seconds.
      reconnectDelay: 60000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    /**
     * Given a successful connection to the backend broker, subscribe to the `details` topic
     *
     * @param frame
     */
    stompClient.onConnect = function (frame) {
      stompClient.subscribe(websocketConfig.topic, function (message) {
        try {
          // Given a new received message, convert it to a ShipDetails instance and update the state
          const apiResponse = JSON.parse(message.body) as APIResponseItem;
          const shipDetails =
            ShipService.extractCurrentShipDetails(apiResponse);
          ShipUpdateBuffer.addToBuffer(shipDetails);
        } catch (error) {
          ErrorNotificationService.addWarning("Data fetching error");
        }
      });
    };

    /**
     * Given that the backend closes the Websocket connection, report it.
     * @param closeEvent the caught event
     */
    stompClient.onWebSocketClose = function (closeEvent) {
      ErrorNotificationService.addWarning("Websocket connection error");
      ShipUpdateBuffer.resetBuffer();
      setShips(new Map());
    };

    /**
     * Leveraging the beforeConnect hook we fetch the latest state of the
     * backend table storing ship details before opening a WebSocket connection
     * with the backend STOMP broker
     */
    stompClient.beforeConnect = async () => {
      console.log("stomp before connect");
      ShipUpdateBuffer.resetBuffer();

      ShipService.queryBackendForShipsArray().then(
        (shipsArray: ShipDetails[]) => {
          setShips(ShipService.constructMap(shipsArray));
        },
      );
    };

    stompClient.onStompError = function (frame) {
      ErrorNotificationService.addWarning(
        "Websocket broker error: " + frame.headers["message"],
      );
    };

    // Initiate the connection with the backend-hosted STOMP broker
    stompClient.activate();

    return () => {
      if (stompClient) {
        stompClient.deactivate();
      }
    };
  }, []);

  return ships;
};

export default useWebSocketClient;
