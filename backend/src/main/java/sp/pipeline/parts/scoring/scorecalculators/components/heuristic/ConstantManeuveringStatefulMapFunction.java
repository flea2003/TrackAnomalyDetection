package sp.pipeline.parts.scoring.scorecalculators.components.heuristic;

import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.*;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.configuration.Configuration;
import sp.model.AISSignal;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public class ConstantManeuveringStatefulMapFunction extends HeuristicStatefulMapFunction {

    private static final float TIME_FRAME_IN_MINUTES = 60f;
    private static final float HEADING_DIFFERENCE_THRESHOLD = 40f;
    private static final int TURNS_COUNT_THRESHOLD = 10;

    private transient ListState<AISSignal> previousSignalsListState;

    /**
     * Initializes the function by initializing the value states (calls super class
     * `open` method) and by initializing the list state for the previous AIS signals.
     * These signals are needed for maneuvering heuristic calculation.
     *
     * @param parameters Flink configuration object
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        previousSignalsListState = getListState("previousSignals", new TypeHint<>() {});
    }

    /**
     * Checks if the current signal is an anomaly.
     * Ship is considered an anomaly when it makes more than 10 turns which are too big.
     * The turn is considered too big when the difference among the headings is more than 40 degrees.
     *
     * @param currentSignal current AIS signal
     * @param pastSignal past AIS signal (non-null object)
     * @return AnomalyScoreWithExplanation object which indicates whether the current
     *     signal is an anomaly. If it is an anomaly, an explanation string and anomaly score
     *     are also included in the same return object.
     */
    @Override
    protected AnomalyScoreWithExplanation checkForAnomaly(AISSignal currentSignal, AISSignal pastSignal) throws Exception {
        previousSignalsListState.add(currentSignal);
        List<AISSignal> recentSignals = updateAndGetRecentAISSignals(currentSignal.getTimestamp());
        long turnCount = countStrongTurns(recentSignals);

        boolean isAnomaly = false;
        String explanation = "";
        DecimalFormat df = getDecimalFormatter();

        if (turnCount > TURNS_COUNT_THRESHOLD) {
            isAnomaly = true;
            explanation += "Maneuvering is too frequent: " + turnCount
                    + " strong turns (turns of more than " + df.format(HEADING_DIFFERENCE_THRESHOLD)
                    + " degrees) during the last " + df.format(TIME_FRAME_IN_MINUTES) + " minutes"
                    + " is more than threshold of " + TURNS_COUNT_THRESHOLD
                    + " turns" + explanationEnding();
        }

        return new AnomalyScoreWithExplanation(isAnomaly, getAnomalyScore(), explanation);
    }

    /**
     * Gets the most recent signals from the list state and updates the list state to store only them.
     * The signal is considered to be recent if it's not more than one hour before of the current time.
     *
     * @param currentTime OffsetDateTime object representing the time with which the signals
     *                    are checked whether they are recent or not
     * @return the list of most recent AIS signals
     * @throws Exception if the Exception is thrown by methods that deal with list state
     */
    private List<AISSignal> updateAndGetRecentAISSignals(OffsetDateTime currentTime) throws Exception {
        Iterable<AISSignal> signalsIterable = previousSignalsListState.get();

        List<AISSignal> recentSignals = StreamSupport.stream(signalsIterable.spliterator(), false)
                .filter(pastSignal -> timeDiffInMinutes(pastSignal.getTimestamp(), currentTime) <= TIME_FRAME_IN_MINUTES)
                .toList();

        previousSignalsListState.update(recentSignals); // only save the recent ones

        return recentSignals;
    }

    /**
     * Counts how many strong turns there are among the given signals.
     * Strong turns are those that had more than 40 degrees difference in signal heading.
     * The heading differences are concatenated before counting this.
     *
     * @param signals the list of AIS signals
     * @return the number of how many strong turns the ship made
     */
    private long countStrongTurns(List<AISSignal> signals) {
        List<Float> turnAmplitudes = getListOfDifferences(signals);
        List<Float> concatenatedTurnAmplitudes = concatenateListOfDifferences(turnAmplitudes);

        return concatenatedTurnAmplitudes.stream()
                .filter(amplitude -> Math.abs(amplitude) > HEADING_DIFFERENCE_THRESHOLD)
                .count();
    }

    /**
     * Gets the list of heading difference among each pair of consecutive signals.
     *
     * @param signals the list of ship's AIS signals
     * @return the list of heading differences
     */
    private List<Float> getListOfDifferences(List<AISSignal> signals) {
        List<Float> turnAmplitudes = new ArrayList<>();
        for (int i = 0; i < signals.size() - 1; i++) {
            AISSignal signal1 = signals.get(i);
            AISSignal signal2 = signals.get(i + 1);

            turnAmplitudes.add(getCorrectedHeading(signal1) - getCorrectedHeading(signal2));
        }

        return turnAmplitudes;
    }

    /**
     * Concatenates the list of differences. Concatenation means that the differences
     * of the same sign are concatenated into a single number. For example, if differences
     * were [-1, -2, 3, 4, 5], then they are concatenated into [-3, 12].
     *
     * @param differences the list of heading differences
     * @return the list of concatenated differences
     */
    private List<Float> concatenateListOfDifferences(List<Float> differences) {
        List<Float> concatenated = new ArrayList<>();
        if (differences.isEmpty()) {
            return concatenated;
        }

        float curSum = differences.get(0);
        for (int i = 1; i < differences.size(); i++) {
            float d = differences.get(i);

            if (areSignsDifferent(curSum, d)) {
                concatenated.add(curSum);
                curSum = 0;
            }

            curSum += d;
        }

        concatenated.add(curSum);

        return concatenated;
    }

    /**
     * Checks if the signs of two floating-point numbers are the same or not.
     *
     * @param a first floating-point number
     * @param b second floating-point number
     * @return true if the signs are different, false otherwise
     */
    private boolean areSignsDifferent(float a, float b) {
        return Math.signum(a) != Math.signum(b);
    }


    /**
     * Gets the anomaly score of the heuristic. This score is given to the ship that
     * is considered an anomaly based on the heuristic.
     *
     * @return the anomaly score of the heuristic
     */
    @Override
    protected float getAnomalyScore() {
        return 25f;
    }

}
