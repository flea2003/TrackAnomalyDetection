SELECT "maxAnomalyScoreInfo", "currentAnomalyInformation", "currentAISSignal"
FROM "ship-details"
WHERE JSON_VALUE("currentAISSignal", '$.id') = ?
ORDER BY "__time"