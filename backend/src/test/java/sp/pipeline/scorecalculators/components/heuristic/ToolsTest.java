package sp.pipeline.scorecalculators.components.heuristic;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ToolsTest {

    @Test
    void harvesineDistanceTest() {
        assertThat(Math.round(Tools.harvesineDistance(10.0f, 20.0f, 30.0f, 40.0f))).isEqualTo(3041);
        assertThat(Math.round(Tools.harvesineDistance( 30.0f, 40.0f, 10.0f, 20.0f))).isEqualTo(3041);
    }
}