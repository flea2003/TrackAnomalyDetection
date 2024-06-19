package sp.unit.pipeline.parts.scoring.scorecalculators.components.heuristic;

import static org.assertj.core.api.Assertions.assertThat;
import static sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools.circularMetric;

import org.junit.jupiter.api.Test;
import sp.pipeline.parts.scoring.scorecalculators.components.heuristic.Tools;

class ToolsTest {

    @Test
    void harvesineDistanceTest() {
        assertThat(Math.round(Tools.harvesineDistance(10.0f, 20.0f, 30.0f, 40.0f))).isEqualTo(1642);
        assertThat(Math.round(Tools.harvesineDistance( 30.0f, 40.0f, 10.0f, 20.0f))).isEqualTo(1642);
    }

    @Test
    void circularMetricTest(){
        assertThat(Math.round(circularMetric(33.4f, 85.3f))).isEqualTo(52);
    }

    @Test
    void constructorTest() {
        Tools t = new Tools();
        assertThat(t).isNotNull();
    }

}