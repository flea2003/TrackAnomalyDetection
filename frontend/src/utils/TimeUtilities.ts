/**
 * Given the timestamp of the lat received AIS signal, compute the time difference
 * between the respective timestamp and the live time and convert the difference to
 * a human-readable string
 * @param timestamp - string representation of the ISO-8601 timestamp
 */
function computeTimeDifference (timestamp: string) {
  const signalTime = new Date(timestamp);
  const timeDifference = (new Date().getTime() - signalTime.getTime());
  const days = Math.floor(timeDifference / (1000*60*60*24));
  const hours = Math.floor((timeDifference % (1000*60*60*24) / (1000*60*60)));
  const minutes = Math.floor(timeDifference % (1000*60*60) / (1000*60));

  return `${days}d, ${hours}h, ${minutes}m`
}

export { computeTimeDifference };