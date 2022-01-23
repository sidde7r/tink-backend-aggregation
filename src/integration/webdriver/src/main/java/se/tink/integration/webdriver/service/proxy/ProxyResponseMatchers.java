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
    public static class ProxyUrlSubstringMatcher implements ProxySaveResponseMatcher {

        private final String urlSubstring;

        @Override
        public boolean matchesResponse(ProxyResponse proxyResponse) {
            String responseUrl = proxyResponse.getMessageInfo().getUrl();
            return StringUtils.containsIgnoreCase(responseUrl, urlSubstring);
        }
    }
}
