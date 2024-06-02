import ErrorNotificationService from "../services/ErrorNotificationService";

class TimeUtilities {
  /**
   * Given the timestamp of the lat received AIS signal, compute the time difference
   * between the respective timestamp and the live time and convert the difference to
   * a human-readable string
   * @param timestamp - string representation of the ISO-8601 timestamp
   */
  static computeTimeDifference(timestamp: string) {
    const signalTime = new Date(timestamp);
    if (Number.isNaN(signalTime.valueOf())) {
      ErrorNotificationService.addWarning("Invalid timestamp format");
      return "Not available";
    }

    const timeDifference =
      TimeUtilities.getCurrentTime().getTime() - signalTime.getTime();
    if (timeDifference <= 0) {
      ErrorNotificationService.addWarning("Invalid timestamp value");
      return "Not available";
    }

    const days = Math.floor(timeDifference / (1000 * 60 * 60 * 24));
    const hours = Math.floor(
      (timeDifference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60),
    );
    const minutes = Math.floor(
      (timeDifference % (1000 * 60 * 60)) / (1000 * 60),
    );

    return `${days}d, ${hours}h, ${minutes}m`;
  }

  /**
   * Utility method that returns the current time.
   */
  static getCurrentTime = () => {
    return new Date();
  };
}
export default TimeUtilities;
