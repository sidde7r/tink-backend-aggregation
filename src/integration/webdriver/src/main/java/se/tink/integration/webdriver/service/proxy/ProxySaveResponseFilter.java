package se.tink.integration.webdriver.service.proxy;

import com.google.common.base.Preconditions;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;

public class ProxySaveResponseFilter implements ProxyFilter {

    private final CountDownLatch responseLatch;
    private ProxyResponse savedResponse;

    private final ProxySaveResponseMatcher proxySaveResponseMatcher;

    public ProxySaveResponseFilter(ProxySaveResponseMatcher proxySaveResponseMatcher) {
        this.responseLatch = new CountDownLatch(1);
        this.proxySaveResponseMatcher = proxySaveResponseMatcher;

        Preconditions.checkNotNull(
                proxySaveResponseMatcher, "Proxy response matcher cannot be null");
    }

    @Override
    public void handleRequest(ProxyRequest request) {
        // ignore
    }

    @Override
    public void handleResponse(ProxyResponse response) {
        if (!proxySaveResponseMatcher.matchesResponse(response)) {
            return;
        }
        // for predictability, we want to listen only for the very first response
        if (savedResponse == null) {
            savedResponse = response;
            responseLatch.countDown();
        }
    }

    public boolean hasResponse() {
        return savedResponse != null;
    }

    public ProxyResponse getResponse() {
        if (!hasResponse()) {
            throw new IllegalStateException("No response saved");
        }
        return savedResponse;
    }

    @SneakyThrows
    public Optional<ProxyResponse> waitForResponse(int waitForSeconds, TimeUnit timeUnit) {
        boolean hasResponse = responseLatch.await(waitForSeconds, timeUnit);
        return hasResponse ? Optional.of(savedResponse) : Optional.empty();
    }
}
