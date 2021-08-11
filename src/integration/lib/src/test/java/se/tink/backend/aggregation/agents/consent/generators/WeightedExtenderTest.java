package se.tink.backend.aggregation.agents.consent.generators;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import se.tink.backend.aggregation.agents.consent.Scope.Weighted;

public class WeightedExtenderTest {

    @Test
    public void shouldNotExtend() {
        WeightedExtender<TestWeighted> extender =
                new WeightedExtender<>(EnumSet.allOf(TestWeighted.class));

        Weighted weighted =
                extender.extendIfNotAvailable(TestWeighted.A, EnumSet.allOf(TestWeighted.class));

        assertThat(weighted).isEqualTo(TestWeighted.A);
    }

    @Test
    public void shouldExtend() {
        WeightedExtender<TestWeighted> extender =
                new WeightedExtender<>(EnumSet.allOf(TestWeighted.class));

        Weighted weighted =
                extender.extendIfNotAvailable(TestWeighted.A, EnumSet.of(TestWeighted.D));

        assertThat(weighted).isEqualTo(TestWeighted.D);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowException() {
        WeightedExtender<TestWeighted> extender =
                new WeightedExtender<>(EnumSet.allOf(TestWeighted.class));

        extender.extendIfNotAvailable(TestWeighted.A, EnumSet.of(TestWeighted.B));
    }

    @RequiredArgsConstructor
    @Getter
    private enum TestWeighted implements Weighted<TestWeighted> {
        A(0),
        B(0),
        C(1),
        D(2);

        private final int weight;

        @Override
        public TestWeighted extendIfNotAvailable(Set<TestWeighted> availableScopes) {
            return null;
        }
    }
}
