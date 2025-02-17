import ErrorNotificationService from "../../services/ErrorNotificationService";
import connectionSettings from "../../configs/connectionSettings.json";

/**
 * Utility class that handles request sending to the backend
 */
class HttpSender {
  static url = connectionSettings.backendURL;

  /**
   * Send an asynchronous GET request to the backend server
   * mentioning the desired endpoint
   * @param endpoint endpoint accessed
   */
  // eslint-disable-next-line
  static async get(endpoint: string): Promise<any> {
    try {
      const response = await fetch(this.url + endpoint);
      if (!response.ok) {
        ErrorNotificationService.addError("Error while fetching");
      }
      return await response.json();
    } catch (error) {
      ErrorNotificationService.addError(
        "Error while fetching " + endpoint + ": " + (error as Error).message,
      );

      return null;
    }
  }

  static async put(endpoint: string): Promise<void> {
    try {
      const response = await fetch(this.url + endpoint, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({}),
      });
      if (!response.ok) {
        ErrorNotificationService.addWarning("Error while fetching " + endpoint);
      }
    } catch (error) {
      ErrorNotificationService.addError(
        "Error while fetching " + endpoint + ": " + (error as Error).message,
      );
    }
  }
}

export default HttpSender;
