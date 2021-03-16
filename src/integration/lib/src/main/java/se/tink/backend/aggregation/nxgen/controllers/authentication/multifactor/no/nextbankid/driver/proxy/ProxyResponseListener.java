package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.proxy;

import com.browserup.bup.filters.ResponseFilter;
import com.browserup.bup.util.HttpMessageContents;
import com.browserup.bup.util.HttpMessageInfo;
import io.netty.handler.codec.http.HttpResponse;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public class ProxyResponseListener implements ResponseFilter {

    private String urlSubstringToListenFor = null;
    private ResponseSynchronizedContainer responseSynchronizedContainer =
            new ResponseSynchronizedContainer();

    /**
     * NOTE: all responses for requests made by browser will go through this code first before they
     * reach browser back. This means that all debug breakpoints put here may cause browser requests
     * to fail because of timeout. Also, when this code hangs a on breakpoint, some websites may not
     * be interactable.
     *
     * <p>Additionally, the JavaScript that initializes BankID iframe is very sensitive to any
     * network delays for first few http requests. If you stop on a breakpoint here before iframe is
     * fully initialized, it will almost certainly interpret it as some network issues and cause
     * iframe initialization to fail (probably due to some security reasons).
     *
     * <p>Finally, although this code will be run in a separate thread, this thread can also be
     * stopped by some breakpoints in other parts of application. The default IntelliJ breakpoint
     * setting is to stop all threads. Again, this may cause BankID iframe initialization to fail.
     * To change this behavior: right click on any breakpoint, select "Thread" radio button (not
     * "All") and click "Make default" button. This way breakpoints in other parts of application
     * will not stop this thread (but you may also have to disable already existing ones).
     */
    @Override
    public void filterResponse(
            HttpResponse response, HttpMessageContents contents, HttpMessageInfo messageInfo) {

        String responseUrl = messageInfo.getUrl();
        if (StringUtils.containsIgnoreCase(responseUrl, urlSubstringToListenFor)) {
            ResponseFromProxy proxyResponse =
                    ResponseFromProxy.builder()
                            .response(response)
                            .contents(contents)
                            .messageInfo(messageInfo)
                            .build();
            responseSynchronizedContainer.saveResponse(proxyResponse);
        }
    }

    public void changeUrlSubstringToListenFor(String urlSubstring) {
        urlSubstringToListenFor = urlSubstring;
        // remove previously saved response if we change the url address that we listen to
        responseSynchronizedContainer = new ResponseSynchronizedContainer();
    }

    public Optional<ResponseFromProxy> waitForResponse(int waitFor, TimeUnit timeUnit) {
        return responseSynchronizedContainer.waitForResponse(waitFor, timeUnit);
    }

    /**
     * Object shared between main application thread and the thread that filters responses from
     * proxy.
     */
    private static class ResponseSynchronizedContainer {

        private final CountDownLatch countDownLatch = new CountDownLatch(1);
        private ResponseFromProxy savedResponse;

        private void saveResponse(ResponseFromProxy response) {
            // we want to listen only for the very first response
            if (savedResponse == null) {
                savedResponse = response;
                countDownLatch.countDown();
            }
        }

        @SneakyThrows
        private Optional<ResponseFromProxy> waitForResponse(int waitForSeconds, TimeUnit timeUnit) {
            boolean hasResponse = countDownLatch.await(waitForSeconds, timeUnit);
            return hasResponse ? Optional.of(savedResponse) : Optional.empty();
        }
    }
}
