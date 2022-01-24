package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.codeapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.codeapp.rpc.MitIdCodeAppPollResponse;
import se.tink.integration.webdriver.service.proxy.ProxyListener;
import se.tink.integration.webdriver.service.proxy.ProxyRequest;
import se.tink.integration.webdriver.service.proxy.ProxyResponse;

public class MitIdCodeAppPollingProxyListener implements ProxyListener {

    @SuppressWarnings("squid:S1075")
    private static final String POLL_URL_PATH = "/poll";

    private final CountDownLatch pollingFinishedLatch = new CountDownLatch(1);
    private MitIdCodeAppPollingResult pollingResult;

    @SneakyThrows
    public Optional<MitIdCodeAppPollingResult> waitForResult(int waitForSeconds) {
        boolean hasResponse = pollingFinishedLatch.await(waitForSeconds, TimeUnit.SECONDS);
        return hasResponse ? Optional.of(pollingResult) : Optional.empty();
    }

    @Override
    public void handleRequest(ProxyRequest request) {
        // ignore
    }

    @Override
    public void handleResponse(ProxyResponse response) {
        String url = response.getMessageInfo().getUrl();
        boolean matchingUrl = url.contains(POLL_URL_PATH);
        if (!matchingUrl) {
            return;
        }

        String httpMethod = response.getMessageInfo().getOriginalRequest().method().name();
        boolean matchingHttpMethod = httpMethod.equals("POST");
        if (!matchingHttpMethod) {
            return;
        }

        pollingResult = getPollingResult(response);
        if (pollingResult != MitIdCodeAppPollingResult.POLLING) {
            pollingFinishedLatch.countDown();
        }
    }

    @SneakyThrows
    private MitIdCodeAppPollingResult getPollingResult(ProxyResponse response) {
        int status = response.getResponse().status().code();
        if (status == 404) {
            return MitIdCodeAppPollingResult.TECHNICAL_ERROR;
        }

        String body = response.getContents().getTextContents();
        MitIdCodeAppPollResponse pollResponse =
                new ObjectMapper().readValue(body, MitIdCodeAppPollResponse.class);

        if (pollResponse.isStillPolling()) {
            return MitIdCodeAppPollingResult.POLLING;
        }

        if (pollResponse.isAccepted()) {
            return MitIdCodeAppPollingResult.OK;
        }

        if (pollResponse.isRejected()) {
            return MitIdCodeAppPollingResult.REJECTED;
        }

        if (pollResponse.isExpired()) {
            return MitIdCodeAppPollingResult.EXPIRED;
        }

        return MitIdCodeAppPollingResult.UNKNOWN;
    }
}
