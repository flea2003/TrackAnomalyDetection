package sp.pipeline.scorecalculators.components.heuristic;

import static org.assertj.core.api.Assertions.assertThat;
import static sp.pipeline.scorecalculators.components.heuristic.Tools.circularMetric;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ToolsTest {

    @Test
    void harvesineDistanceTest() {
        assertThat(Math.round(Tools.harvesineDistance(10.0f, 20.0f, 30.0f, 40.0f))).isEqualTo(3041);
        assertThat(Math.round(Tools.harvesineDistance( 30.0f, 40.0f, 10.0f, 20.0f))).isEqualTo(3041);
    }

    @Test
    void circularMetricTest(){
        assertThat(Math.round(circularMetric(33.4f, 85.3f))).isEqualTo(52);
    }

}