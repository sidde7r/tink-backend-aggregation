package se.tink.integration.webdriver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;

public class ChromeDriverInitializerTest {

    @Test
    public void getListArgumentsShouldReturnArgumentsIncludingHeadlessMode() {
        // given
        // when
        List<String> arguments =
                ChromeDriverInitializer.getListArguments("dummyAgent", "dummyLanguage");

        // then
        assertThat(arguments).hasSize(13);
        assertThat(arguments).contains("--headless");
        assertThat(arguments).contains("--blink-settings=imagesEnabled=false");
    }
}
