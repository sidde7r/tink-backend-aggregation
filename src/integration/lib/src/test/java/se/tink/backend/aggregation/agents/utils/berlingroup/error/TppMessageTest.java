package se.tink.backend.aggregation.agents.utils.berlingroup.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TppMessageTest {

    private final TppMessage TEST_TPP_MESSAGE =
            TppMessage.builder().category("ERROR").code("code").path("path").text("text").build();

    @Test
    public void matchesShouldReturnTrueWhenMatchingToTppMessageWithAllNulLFields() {
        // given
        TppMessage knownError = new TppMessage();

        // when
        boolean result = TEST_TPP_MESSAGE.matches(knownError);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void matchesShouldMatchIgnoringCase() {
        // given
        TppMessage knownError = TppMessage.builder().text("TEXT").build();

        // when
        boolean result = TEST_TPP_MESSAGE.matches(knownError);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void matchesShouldNotMatchWhenComparingByFieldThatDoesNotMatch() {
        // given
        TppMessage knownError = TppMessage.builder().text("different text for sure").build();

        // when
        boolean result = TEST_TPP_MESSAGE.matches(knownError);

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void matchesShouldMatchItself() {
        // given

        // when
        boolean result = TEST_TPP_MESSAGE.matches(TEST_TPP_MESSAGE);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void matchesShouldNotMatchWhenAtLeastOneDifference() {
        // given
        TppMessage knownError =
                TppMessage.builder()
                        .category("ErroR")
                        .code("codE")
                        .path("pAth OOPS")
                        .text("text")
                        .build();
        // when
        boolean result = TEST_TPP_MESSAGE.matches(knownError);

        // then
        assertThat(result).isFalse();
    }
}
