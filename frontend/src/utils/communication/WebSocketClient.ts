import { useEffect, useState } from "react";
import ShipDetails from "../../model/ShipDetails";
import { Client } from "@stomp/stompjs";
import APIResponseItem from "../../templates/APIResponseItem";
import ShipService from "../../services/ShipService";
import ErrorNotificationService from "../../services/ErrorNotificationService";

const useWebSocketClient = () => {
  const [ships, setShips] = useState<Map<number, ShipDetails>>(new Map());

  /**
   * Configure the WebSocket connection.
   */
  useEffect(() => {
    const stompClient = new Client({
      brokerURL: "ws://localhost:8081/details",
      debug: function (str) {
        console.log(str);
      },
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
      stompClient.subscribe("/topic/details", function (message) {
        try {
          /**
           * Given a new received message, convert it to a ShipDetails instance and update the state.
           */
          const apiResponse = JSON.parse(message.body) as APIResponseItem;
          const shipDetails =
            ShipService.extractCurrentShipDetails(apiResponse);
          setShips((prevShips) => {
            return new Map(prevShips).set(shipDetails.id, shipDetails);
          });
        } catch (error) {
          ErrorNotificationService.addError("Data fetching error");
          setShips((prevMap) => {
            return new Map();
          });
        }
      });
    };

    /**
     * Given that the backend closes the Websocket connection, report it.
     * @param closeEvent the caught event
     */
    stompClient.onWebSocketClose = function (closeEvent) {
      ErrorNotificationService.addError("Websocket connection error");
      setShips((prevMap) => {
        return new Map();
      });
    };

    /**
     * Leveraging the beforeConnect hook we fetch the latest state of the
     * backend table storing ship details before opening a WebSocket connection
     * with the backend STOMP broker
     */
    stompClient.beforeConnect = async () => {
      ShipService.queryBackendForShipsArray().then(
        (shipsArray: ShipDetails[]) => {
          setShips(ShipService.constructMap(shipsArray));
        },
      );
    };

    stompClient.onStompError = function (frame) {
      ErrorNotificationService.addError(
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
