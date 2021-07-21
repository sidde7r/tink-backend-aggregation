package se.tink.integration.webdriver;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.Proxy;

@Getter
@Builder(builderClassName = "ChromeDriverConfigBuilder")
@RequiredArgsConstructor
public class ChromeDriverConfig {

    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36";
    private static final String DEFAULT_ACCEPT_LANGUAGE = "en-US";
    private static final long DEFAULT_TIMEOUT_SECONDS = 30;

    private final String userAgent;
    private final String acceptLanguage;
    private final long timeoutInSeconds;
    private final Proxy proxy;

    public static ChromeDriverConfig defaultConfig() {
        return ChromeDriverConfig.builder().build();
    }

    /** Override lombok generated builder to add defaults */
    public static class ChromeDriverConfigBuilder {

        public ChromeDriverConfigBuilder() {
            this.userAgent = DEFAULT_USER_AGENT;
            this.acceptLanguage = DEFAULT_ACCEPT_LANGUAGE;
            this.timeoutInSeconds = DEFAULT_TIMEOUT_SECONDS;
        }
    }
}
