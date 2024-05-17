/**
 * Given an anomaly score, this function calculates the color of the anomaly list entry.
 *
 * @param shipAnomalyScore the anomaly score of the ship
 * @returns the color of the anomaly list entry
 */
function calculateAnomalyColor(shipAnomalyScore: number) {
  shipAnomalyScore /= 100.0;
  const green = Math.floor(255 * (1 - shipAnomalyScore));
  const red = Math.floor(255 * shipAnomalyScore);
  const alpha = 0.4;
  return `rgba(${red}, ${green}, 0, ${alpha})`;
}

export { calculateAnomalyColor };
