package sp.pipeline.parts.scoring.scorecalculators.components.heuristic;

import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.timeDiffInMinutes;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.OffsetDateTime;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import sp.model.AnomalyInformation;
import sp.model.AISSignal;

@Getter
public abstract class HeuristicStatefulMapFunction extends RichMapFunction<AISSignal, AnomalyInformation> {

    // last processed AIS signal
    private transient ValueState<AISSignal> aisSignalValueState;

    // the anomaly information for the last detected anomaly
    // this is sometimes the same as anomalyInformationValueState, but sometimes different
    private transient ValueState<AnomalyInformation> lastDetectedAnomalyValueState;

    /**
     * Checks if the current signal is an anomaly. If the heuristic needs,
     * the current signal is compared with the given past signal.
     *
     * @param currentSignal current AIS signal
     * @param pastSignal past AIS signal (non-null object)
     * @return AnomalyScoreWithExplanation object which indicates whether the current
     *     signal is an anomaly. If it is an anomaly, an explanation string and anomaly score
     *     are also included in the same return object.
     */
    protected abstract AnomalyScoreWithExplanation checkForAnomaly(AISSignal currentSignal, AISSignal pastSignal)
            throws Exception;

    /**
     * Gets the anomaly score of the heuristic. This score is given to the ship that
     * is considered an anomaly based on the heuristic.
     *
     * @return the anomaly score of the heuristic
     */
    protected abstract float getAnomalyScore();

    /**
     * Helper function for the ending of the explanation string.
     * The ending is a dot symbol followed by new line symbol.
     *
     * @return the ending string
     */
    protected String explanationEnding() {
        return ".\n";
    }

    /**
     * DecimalFormatter for writing floating-point numbers in explanation strings.
     * The format is configured to allow maximum two decimal places.
     * The used locale in the formatter is `Locale.ENGLISH`, so that dot is used as a
     * decimal separator.
     *
     * @return the formatter object
     */
    protected DecimalFormat getDecimalFormatter() {
        return new DecimalFormat(
                "#.##",
                new DecimalFormatSymbols(Locale.ENGLISH)
        );
    }

    /**
     * Initializes the function by initializing the value states.
     *
     * @param parameters Flink configuration object
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        aisSignalValueState = getValueState("aisSignal", new TypeHint<>() {});
        lastDetectedAnomalyValueState = getValueState("lastDetectedAnomaly", new TypeHint<>() {});
    }

    /**
     * Helper method for creating (initializing) a value state.
     *
     * @param name name of the state
     * @param typeHint type hint for the descriptor (cannot be extracted since Flink then
     *                 shows errors about generic method)
     * @param <T> the type of the objects stored in the value state
     * @return the created value state
     */
    private <T> ValueState<T> getValueState(String name, TypeHint<T> typeHint) {
        ValueStateDescriptor<T> descriptor = new ValueStateDescriptor<>(
                name, TypeInformation.of(typeHint)
        );

        return getRuntimeContext().getState(descriptor);
    }

    /**
     * Helper method for creating (initializing) a list state.
     *
     * @param name name of the state
     * @param typeHint type hint for the descriptor (cannot be extracted since Flink then
     *                 shows errors about generic method)
     * @param <T> the type of the objects stored in the list state
     * @return the created list state
     */
    protected <T> ListState<T> getListState(String name, TypeHint<T> typeHint) {
        ListStateDescriptor<T> descriptor = new ListStateDescriptor<>(
                name, TypeInformation.of(typeHint)
        );

        return getRuntimeContext().getListState(descriptor);
    }

    /**
     * Performs a stateful map operation that receives an AIS signal and produces an
     * AnomalyInformation based on the predefined heuristics for the speed of the ship.
     *
     * @param value The input value (AIS signal)
     * @return the computed Anomaly Information object
     * @exception Exception exception thrown by interaction with value states
     */
    @Override
    public AnomalyInformation map(AISSignal value) throws Exception {
        checkCurrentSignal(value);

        AnomalyInformation anomalyInfo;
        AnomalyInformation lastDetectedAnomaly = getLastDetectedAnomalyValueState().value();

        if (isLastDetectedAnomalyRecent(lastDetectedAnomaly, value.getTimestamp())) {
            anomalyInfo = new AnomalyInformation(
                    lastDetectedAnomaly.getScore(), lastDetectedAnomaly.getExplanation(),
                    value.getTimestamp(), value.getId()
            );
        } else {
            anomalyInfo = new AnomalyInformation(
                    0f, "", value.getTimestamp(), value.getId()
            );
        }

        // save the AIS signal in the value state for the upcoming signal
        this.getAisSignalValueState().update(value);

        return anomalyInfo;
    }

    /**
     * Check if the last detected anomaly is still relevant. It's considered still relevant
     * if it is in the past 30 minutes.
     *
     * @param recentAnomaly last detected anomaly information
     * @param currentTime time of the current signal
     * @return true if the last detected anomaly is not earlier than 30 minutes
     *     before the current signal
     */
    private boolean isLastDetectedAnomalyRecent(AnomalyInformation recentAnomaly, OffsetDateTime currentTime) {
        if (recentAnomaly == null) {
            return false;
        }

        return timeDiffInMinutes(recentAnomaly.getCorrespondingTimestamp(), currentTime) <= 30;
    }

    /**
     * Checks the current signal. If it is an anomaly, then new anomaly information is saved
     * in the value state for the last detected anomaly.
     *
     * @param currentSignal current AIS signal
     * @throws Exception if interaction with value states throw exception
     */
    private void checkCurrentSignal(AISSignal currentSignal) throws Exception {
        AISSignal pastSignal = getAisSignalValueState().value();
        AnomalyScoreWithExplanation result = checkForAnomaly(currentSignal, pastSignal);

        if (result.isAnomaly()) {
            AnomalyInformation anomalyInfo = new AnomalyInformation(
                    result.getAnomalyScore(), result.getExplanation(),
                    currentSignal.getTimestamp(), currentSignal.getId()
            );

            this.lastDetectedAnomalyValueState.update(anomalyInfo);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    protected static class AnomalyScoreWithExplanation {
        private boolean isAnomaly;
        private float anomalyScore;
        private String explanation;
    }
}
