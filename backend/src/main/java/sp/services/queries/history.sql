SELECT *
FROM "ship-details"
WHERE JSON_VALUE("currentAISSignal", '$.id') = '?'
ORDER BY "__time"