package se.tink.integration.webdriver.service.proxy;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProxyResponseMatchers {

    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class ProxyResponseUrlSubstringMatcher implements ProxyResponseMatcher {

        private final String urlSubstring;

        @Override
        public boolean matches(ResponseFromProxy responseFromProxy) {
            String responseUrl = responseFromProxy.getMessageInfo().getUrl();
            return StringUtils.containsIgnoreCase(responseUrl, urlSubstring);
        }
    }
}
