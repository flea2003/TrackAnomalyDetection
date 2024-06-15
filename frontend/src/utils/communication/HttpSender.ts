import ErrorNotificationService from "../../services/ErrorNotificationService";
import TrajectoryResponseItem from "../../templates/TrajectoryResponseItem";
import TrajectoryPoint from "../../model/TrajectoryPoint";
import { randomInt } from "node:crypto";

/**
 * Utility class that handles request sending to the backend
 */
class HttpSender {
  static url = "http://localhost:8180";


  static async getDummyData(): Promise<TrajectoryPoint[]> {

    const singal1 = new TrajectoryPoint(1, Math.floor(Math.random()*20) , Math.floor(Math.random()*20), "", 37)
    const singal2 = new TrajectoryPoint(1, Math.floor(Math.random()*20), 15, "", 37)
    const singal3 = new TrajectoryPoint(1,Math.floor(Math.random()*20), Math.floor(Math.random()*20), "", 37)
    const singal4 = new TrajectoryPoint(1, Math.floor(Math.random()*20), 14, "", 37)
    const singal5 = new TrajectoryPoint(1, 17, 60, "", 37)

    console.log(singal1.longitude);

    /*
    const singal1 = new TrajectoryPoint(1, randomInt(10, 20), 26, "", 37)
    const singal2 = new TrajectoryPoint(1, 24, randomInt(10, 20), "", 37)
    const singal3 = new TrajectoryPoint(1, randomInt(10, 20), 56, "", 37)
    const singal4 = new TrajectoryPoint(1, 22, randomInt(10, 20), "", 37)
    const singal5 = new TrajectoryPoint(1, randomInt(10, 20), 60, "", 37)


     */

    return [singal1, singal2, singal3, singal4, singal5];
  }

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
      if (error instanceof Error)
        ErrorNotificationService.addError(
          "Error while fetching " + endpoint + ": " + error.message,
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
      if (error instanceof Error)
        ErrorNotificationService.addError(
          "Error while fetching " + endpoint + ": " + error.message,
        );
    }
  }
}

export default HttpSender;
