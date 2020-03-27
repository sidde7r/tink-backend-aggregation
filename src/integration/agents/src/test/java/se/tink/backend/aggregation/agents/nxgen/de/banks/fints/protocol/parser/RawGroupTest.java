package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import org.junit.Test;

public class RawGroupTest {

    @Test
    public void shouldReportEmptyRawGroupAsEmpty() {
        // given
        RawGroup rawGroup = new RawGroup();

        // when

        // then
        assertThat(rawGroup).isEmpty();
    }

    @Test
    public void shouldReportOneEmptyStringElementRawGroupAsEmpty() {
        // given
        RawGroup rawGroup = new RawGroup();

        // when
        rawGroup.add("");

        // then
        assertThat(rawGroup).isEmpty();
    }

    @Test
    public void shouldReportOneNullStringElementRawGroupAsEmpty() {
        // given
        RawGroup rawGroup = new RawGroup();

        // when
        rawGroup.add(null);

        // then
        assertThat(rawGroup).isEmpty();
    }

    @Test
    public void shouldReportAnythinElseAsNotEmpty() {
        // given
        RawGroup rawGroup = new RawGroup();

        // when
        rawGroup.add("a");

        // then
        assertThat(rawGroup).isNotEmpty();
    }

    @Test
    public void shouldMapStringsProperly() {
        // given
        RawGroup rawGroup = new RawGroup();

        // when
        rawGroup.add(null);
        rawGroup.add("");
        rawGroup.add("AnyOldString");
        rawGroup.add("ThisIsNotThePlaceThatEscapesOrUnescapesThings????@@@@?@?'");

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
        RawGroup rawGroup = new RawGroup();

        // when
        rawGroup.add(null);
        rawGroup.add("");
        rawGroup.add("J");
        rawGroup.add("N");

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
        RawGroup rawGroup = new RawGroup();
        rawGroup.add("NotAProperBool");

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
        RawGroup rawGroup = new RawGroup();

        // when
        rawGroup.add(null);
        rawGroup.add("");
        rawGroup.add("15606");
        rawGroup.add("-98989865");

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
        RawGroup rawGroup = new RawGroup();
        rawGroup.add("ThisIsNotInteger");

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
        RawGroup rawGroup = new RawGroup();

        // when
        rawGroup.add(null);
        rawGroup.add("");
        rawGroup.add("15505,00452");
        rawGroup.add("-0,231311");
        rawGroup.add("-100,");

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
        RawGroup rawGroup = new RawGroup();
        rawGroup.add("ThisIsNotDecimal");

        // when
        Throwable throwable = catchThrowable(() -> rawGroup.getDecimal(0));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Could not map to decimal: ThisIsNotDecimal");
    }
}
