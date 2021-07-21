package se.tink.integration.webdriver;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder(builderClassName = "PhantomJsConfigBuilder")
@RequiredArgsConstructor
public class PhantomJsConfig {

    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 10_1_1 like Mac OS X) AppleWebKit/602.2.14 (KHTML, like Gecko) Mobile/14B100";
    private static final String DEFAULT_ACCEPT_LANGUAGE = "en-US";
    private static final long DEFAULT_PHANTOMJS_TIMEOUT_SECONDS = 30;

    private final String userAgent;
    private final String acceptLanguage;
    private final long timeoutInSeconds;

    public static PhantomJsConfig defaultConfig() {
        return PhantomJsConfig.builder().build();
    }

    /** Override lombok generated builder to add defaults */
    public static class PhantomJsConfigBuilder {

        public PhantomJsConfigBuilder() {
            this.userAgent = DEFAULT_USER_AGENT;
            this.acceptLanguage = DEFAULT_ACCEPT_LANGUAGE;
            this.timeoutInSeconds = DEFAULT_PHANTOMJS_TIMEOUT_SECONDS;
        }
    }
}
