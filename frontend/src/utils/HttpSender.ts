/**
 * Utility class that handles request sending to the backend
 */
class HttpSender {
  private url = "http://localhost:8081";

  /**
   * Send an asynchronous GET request to the backend server
   * mentioning the desired endpoint
   * @param endpoint - endpoint accessed
   */
  // eslint-disable-next-line
  async get(endpoint: string): Promise<any> {
    try {
      const response = await fetch(this.url + endpoint);
      if (!response.ok) {
        throw new Error("Internal server error");
      }
      return await response.json();
    } catch (error) {
      // Error handling
      // TODO: Implement a smarter way to handle errors
      console.log("Error: " + error);
    }
  }
}

export default HttpSender;
