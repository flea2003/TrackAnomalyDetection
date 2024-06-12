/**
 * Given an anomaly score, this function calculates the color of the anomaly list entry.
 *
 * @param shipAnomalyScore the anomaly score of the ship
 * @param brighter boolean that indicates whether to make the color less transparent
 * @returns the color of the anomaly list entry
 */
function calculateAnomalyColor(shipAnomalyScore: number, brighter: boolean) {
  shipAnomalyScore /= 100.0;
  const green = Math.floor(255 * (1 - shipAnomalyScore));
  const red = Math.floor(255 * shipAnomalyScore);
  const alpha = brighter ? 1 : 0.4;

  return `rgba(${red}, ${green}, 0, ${alpha})`;
}

/**
 * Given an anomaly score, this function calculates the color for the cluster
 * with such an anomaly score. Similar to `calculateAnomalyColor`, but has a
 * different alpha channel value.
 *
 * @param anomalyScore the score of the anomaly for the cluster
 * @returns the color of the cluster
 */
function calculateClusterColor(anomalyScore: number) {
  anomalyScore /= 100.0;
  const green = Math.floor(255 * (1 - anomalyScore));
  const red = Math.floor(255 * anomalyScore);
  const alpha = 0.7;

  return `rgba(${red}, ${green}, 0, ${alpha})`;
}

export { calculateAnomalyColor, calculateClusterColor };
