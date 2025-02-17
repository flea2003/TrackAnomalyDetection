package sp.pipeline.parts.scoring.scorecalculators.components.heuristic;

import sp.model.AISSignal;
import java.time.Duration;
import java.time.OffsetDateTime;

public class Tools {

    /**
     * Util static class that calculates the globe distance (nautical miles) between 2 points.
     *
     * @param lat1 the latitude of the first point.
     * @param lon1 the longitude of the first point.
     * @param lat2 the latitude of the second point.
     * @param lon2 the longitude of the second point.
     * @return the distance on the globe between the 2 points.
     */
    public static float harvesineDistance(float lat1, float lon1, float lat2, float lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double dlon = lon2Rad - lon1Rad;
        double dlat = lat2Rad - lat1Rad;

        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double r = 6371;

        return (float) (r * c * 0.54);
    }

    /**
     * A metric used to compute the difference between the consecutive courses or headings.
     *
     * @param deg1 the course/heading reported in one signal
     * @param deg2 the course/heading reported in another signal
     * @return the computer metric of the difference.
     */
    public static float circularMetric(float deg1, float deg2) {
        return (float) (180.0 - Math.abs(180 - (deg1 % 360 - deg2 % 360 + 360) % 360));
    }

    /**
     * Helper method to get the difference between two timestamps in minutes.
     *
     * @param time1 first timestamp
     * @param time2 second timestamp
     * @return time difference in minutes
     */
    public static long timeDiffInMinutes(OffsetDateTime time1, OffsetDateTime time2) {
        return Duration.between(time1, time2).toMinutes();
    }

    /**
     * Calculates the difference between timestamps from two AIS signals. The difference
     * is calculated in minutes.
     *
     * @param currentSignal the current AIS signal
     * @param pastSignal the past AIS signal
     * @return time difference in minutes
     */
    public static long timeDiffInMinutes(AISSignal currentSignal, AISSignal pastSignal) {
        return timeDiffInMinutes(pastSignal.getTimestamp(), currentSignal.getTimestamp());
    }

    /**
     * Calculates the difference between timestamps from two AIS signals. The difference
     * is calculated in hours.
     *
     * @param currentSignal the current AIS signal
     * @param pastSignal the past AIS signal
     * @return time difference in hours
     */
    public static double timeDiffInHours(AISSignal currentSignal, AISSignal pastSignal) {
        return ((double) timeDiffInMinutes(currentSignal, pastSignal)) / 60.0;
    }

    /**
     * Calculates the distance travelled (nautical miles) from the location of one AIS signal to the location
     * of another.
     *
     * @param currentSignal the current AIS signal
     * @param pastSignal the past AIS signal
     * @return the distance between the positions of two AIS signals
     */
    public static float getDistanceTravelled(AISSignal currentSignal, AISSignal pastSignal) {
        return harvesineDistance(
                currentSignal.getLatitude(), currentSignal.getLongitude(),
                pastSignal.getLatitude(), pastSignal.getLongitude()
        );
    }
}
