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

  // Update the ships based on buffer frequently
  useEffect(() => {
    const updateShips = () => {
      // Copy the map of the current ships
      const newShips = new Map(ships);

      // Update the `newShips` map based on the buffered ships
      ShipUpdateBuffer.getBufferedShipsAndReset().forEach((shipToUpdate) => {
        newShips.set(shipToUpdate.id, shipToUpdate);
      });

      // Update the React state for ships
      setShips(newShips);
    };

    const intervalId = setInterval(
      updateShips,
      websocketConfig.websocketBufferRefreshMs,
    );

    return () => {
      clearInterval(intervalId);
    };
  }, [ships]);

  /**
   * Configure the WebSocket connection.
   */
  useEffect(() => {
    const stompClient = new Client({
      brokerURL: websocketConfig.brokerUrl,
      reconnectDelay: websocketConfig.reconnectDelayMs,
      heartbeatIncoming: websocketConfig.heartbeatIncoming,
      heartbeatOutgoing: websocketConfig.heartbeatOutgoing,
    });

    /**
     * Given a successful connection to the backend broker, subscribe to the `details` topic
     */
    stompClient.onConnect = () => {
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
     */
    stompClient.onWebSocketClose = () => {
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
      ShipUpdateBuffer.resetBuffer();

      ShipService.queryBackendForShipsArray().then(
        (shipsArray: ShipDetails[]) => {
          setShips(ShipService.constructMap(shipsArray));
        },
      );
    };

    stompClient.onStompError = (frame) => {
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
