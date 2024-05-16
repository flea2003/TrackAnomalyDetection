package sp.pipeline.scorecalculators.components.heuristic;

public class Tools {

    /**
     * Util static class that calculates the globe distance between 2 points.
     *
     * @param lat1 - the latitude of the first point.
     * @param lon1 - the longitude of the first point.
     * @param lat2 - the latitude of the second point.
     * @param lon2 - the longitude of the second point.
     * @return - the distance on the globe between the 2 points.
     */
    public static Double harvesineDistance(float lat1, float lon1, float lat2, float lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double dlon = lon2Rad - lon1Rad;
        double dlat = lat2Rad - lat1Rad;

        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double r = 6371;

        return r * c;
    }

}
