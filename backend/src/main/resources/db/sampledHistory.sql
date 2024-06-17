SELECT
    "currentAISSignal" AS aisSignal,
    JSON_VALUE("currentAnomalyInformation", '$.score') AS anomalyScore
FROM
    "ship-details"
WHERE
    JSON_VALUE("currentAISSignal", '$.id') = ?
ORDER BY
    "__time"