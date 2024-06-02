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
    if (!this.isDateValid(signalTime)) return "Not available";
    const timeDifference =
      TimeUtilities.getCurrentTime().getTime() - signalTime.getTime();
    if (timeDifference <= 0) {
      ErrorNotificationService.addError("Invalid timestamp value");
      return "Not available";
    }

    const days = Math.floor(timeDifference / (1000 * 60 * 60 * 24));
    const hours = Math.floor(
      (timeDifference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60),
    );
    const minutes = Math.floor(
      (timeDifference % (1000 * 60 * 60)) / (1000 * 60),
    );

    return `${days}d ${hours}h ${minutes}m`;
  }

  /**
   * Given the timestamp, returns properly formatted string
   * @param timestamp - string representation of the ISO-8601 timestamp
   */
  static reformatTimestamp(timestamp: string) {
    const signalTime = new Date(timestamp);
    if (!this.isDateValid(signalTime)) return "Not available";
    const year = this.prependZero(signalTime.getUTCFullYear());
    const month = this.prependZero(signalTime.getUTCMonth() + 1);
    const day = this.prependZero(signalTime.getUTCDate());
    const hour = this.prependZero(signalTime.getUTCHours());
    const minute = this.prependZero(signalTime.getUTCMinutes());

    return year + "-" + month + "-" + day + " " + hour + ":" + minute;
  }

  /**
   * Method that prepends zero to digits when reformatting
   *
   * @param value integer that is considered for prependind
   */
  static prependZero(value: number) {
    if (value >= 0 && value < 10) return "0" + value;
    else return value + "";
  }

  /**
   * Utility method that returns the current time.
   */
  static getCurrentTime = () => {
    return new Date();
  };

  /**
   * Method that checks if the date is valid
   *
   * @param signalTime date that is being checked
   */
  static isDateValid = (signalTime: Date) => {
    if (Number.isNaN(signalTime.valueOf())) {
      ErrorNotificationService.addError("Invalid timestamp format");
      return false;
    }
    return true;
  };
}
export default TimeUtilities;
