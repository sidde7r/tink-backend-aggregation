package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class RawGroupTest {

    @Test
    public void shouldReportEmptyRawGroupAsEmpty() {
        // given
        RawGroup rawGroup = new RawGroup(Collections.emptyList());

        // when

        // then
        assertThat(rawGroup.isEmpty()).isTrue();
    }

    @Test
    public void shouldReportOneEmptyStringElementRawGroupAsEmpty() {
        // given
        RawGroup rawGroup = new RawGroup(Collections.singletonList(""));

        // when

        // then
        assertThat(rawGroup.isEmpty()).isTrue();
    }

    @Test
    public void shouldReportOneNullStringElementRawGroupAsEmpty() {
        // given
        RawGroup rawGroup = new RawGroup(Collections.singletonList(null));

        // when

        // then
        assertThat(rawGroup.isEmpty()).isTrue();
    }

    @Test
    public void shouldReportAnythingElseAsNotEmpty() {
        // given
        RawGroup rawGroup = new RawGroup(Collections.singletonList("a"));

        // when

        // then
        assertThat(rawGroup.isEmpty()).isFalse();
    }

    @Test
    public void shouldMapStringsProperly() {
        // given
        RawGroup rawGroup =
                new RawGroup(
                        Arrays.asList(
                                null,
                                "",
                                "AnyOldString",
                                "ThisIsNotThePlaceThatEscapesOrUnescapesThings????@@@@?@?'"));

        // when

        // then
        assertThat(rawGroup.getString(0)).isNull();
        assertThat(rawGroup.getString(1)).isNull();
        assertThat(rawGroup.getString(2)).isEqualTo("AnyOldString");
        assertThat(rawGroup.getString(3))
                .isEqualTo("ThisIsNotThePlaceThatEscapesOrUnescapesThings????@@@@?@?'");
        assertThat(rawGroup.getString(500)).isNull();
    }

    @Test
    public void shouldMapBooleansProperly() {
        // given
        RawGroup rawGroup = new RawGroup(Arrays.asList(null, "", "J", "N"));

        // when

        // then
        assertThat(rawGroup.getBoolean(0)).isNull();
        assertThat(rawGroup.getBoolean(1)).isNull();
        assertThat(rawGroup.getBoolean(2)).isTrue();
        assertThat(rawGroup.getBoolean(3)).isFalse();
        assertThat(rawGroup.getBoolean(500)).isNull();
    }

    @Test
    public void shouldThrowWhenFailsToMapBoolean() {
        // given
        RawGroup rawGroup = new RawGroup(Collections.singletonList("NotAProperBool"));

        // when
        Throwable throwable = catchThrowable(() -> rawGroup.getBoolean(0));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Could not map to boolean: NotAProperBool");
    }

    @Test
    public void shouldMapIntegersProperly() {
        // given
        RawGroup rawGroup = new RawGroup(Arrays.asList(null, "", "15606", "-98989865"));

        // when

        // then
        assertThat(rawGroup.getInteger(0)).isNull();
        assertThat(rawGroup.getInteger(1)).isNull();
        assertThat(rawGroup.getInteger(2)).isEqualTo(15606);
        assertThat(rawGroup.getInteger(3)).isEqualTo(-98989865);
        assertThat(rawGroup.getInteger(500)).isNull();
    }

    @Test
    public void shouldThrowWhenFailsToMapInteger() {
        // given
        RawGroup rawGroup = new RawGroup(Collections.singletonList("ThisIsNotInteger"));

        // when
        Throwable throwable = catchThrowable(() -> rawGroup.getInteger(0));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Could not map to integer: ThisIsNotInteger");
    }

    @Test
    public void shouldMapDecimalsProperly() {
        // given
        RawGroup rawGroup =
                new RawGroup(Arrays.asList(null, "", "15505,00452", "-0,231311", "-100,"));

        // when

        // then
        assertThat(rawGroup.getDecimal(0)).isNull();
        assertThat(rawGroup.getDecimal(1)).isNull();
        assertThat(rawGroup.getDecimal(2)).isEqualTo(BigDecimal.valueOf(15505.00452));
        assertThat(rawGroup.getDecimal(3)).isEqualTo(BigDecimal.valueOf(-0.231311));
        assertThat(rawGroup.getDecimal(4)).isEqualTo(BigDecimal.valueOf(-100));
        assertThat(rawGroup.getDecimal(500)).isNull();
    }

    @Test
    public void shouldThrowWhenFailsToMapDecimal() {
        // given
        RawGroup rawGroup = new RawGroup(Collections.singletonList("ThisIsNotDecimal"));

        // when
        Throwable throwable = catchThrowable(() -> rawGroup.getDecimal(0));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Could not map to decimal: ThisIsNotDecimal");
    }
}
