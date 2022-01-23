package se.tink.integration.webdriver.service.proxy;

import static se.tink.integration.webdriver.service.WebDriverConstants.LOG_TAG;

import com.browserup.bup.filters.RequestFilter;
import com.browserup.bup.filters.ResponseFilter;
import com.browserup.bup.util.HttpMessageContents;
import com.browserup.bup.util.HttpMessageInfo;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * NOTE: all request & responses made by browser will go through this code first before they reach
 * browser back. This means that all debug breakpoints put here may cause browser requests to fail
 * because of timeout. Also, when this code hangs a on breakpoint, some websites may not be
 * interactable.
 *
 * <p>Additionally, the JavaScript that initializes BankID iframe is very sensitive to any network
 * delays for first few http requests. If you stop on a breakpoint here before iframe is fully
 * initialized, it will almost certainly interpret it as some network issues and cause iframe
 * initialization to fail (probably due to some security reasons).
 *
 * <p>Finally, although this code will be run in a separate thread, this thread can also be stopped
 * by some breakpoints in other parts of application. The default IntelliJ breakpoint setting is to
 * stop all threads. Again, this may cause BankID iframe initialization to fail. To change this
 * behavior: right click on any breakpoint, select "Thread" radio button (not "All") and click "Make
 * default" button. This way breakpoints in other parts of application will not stop this thread
 * (but you may also have to disable already existing ones).
 */
@Slf4j
public class ProxyFilter implements ResponseFilter, RequestFilter {

    private final Map<String, ProxyListener> proxyListeners = new HashMap<>();

    public void addListener(String key, ProxyListener listener) {
        if (proxyListeners.containsKey(key)) {
            log.warn("{} Overriding Proxy filter for key: {}", LOG_TAG, key);
        }
        proxyListeners.put(key, listener);
    }

    @Override
    public HttpResponse filterRequest(
            HttpRequest httpRequest, HttpMessageContents contents, HttpMessageInfo messageInfo) {
        ProxyRequest proxyRequest =
                ProxyRequest.builder()
                        .request(httpRequest)
                        .contents(contents)
                        .messageInfo(messageInfo)
                        .build();
        preserveHttpBody(contents);

        proxyListeners.forEach(
                (listenerKey, listener) -> {
                    try {
                        listener.handleRequest(proxyRequest);
                    } catch (Exception e) {
                        log.error(
                                "{} Error when handling proxy request in listener: {}",
                                LOG_TAG,
                                listenerKey,
                                e);
                    }
                });

        // do not "short-circuit" the HTTP exchange
        return null;
    }

    @Override
    public void filterResponse(
            HttpResponse response, HttpMessageContents contents, HttpMessageInfo messageInfo) {
        ProxyResponse proxyResponse =
                ProxyResponse.builder()
                        .response(response)
                        .contents(contents)
                        .messageInfo(messageInfo)
                        .build();
        preserveHttpBody(contents);

        proxyListeners.forEach(
                (listenerKey, listener) -> {
                    try {
                        listener.handleResponse(proxyResponse);
                    } catch (Exception e) {
                        log.error(
                                "{} Error when handling proxy response in listener: {}",
                                LOG_TAG,
                                listenerKey,
                                e);
                    }
                });
    }

    private void preserveHttpBody(HttpMessageContents contents) {
        /*
        The HttpMessageContents we got in filter is passed to all listeners as a reference.
        Listeners may then save it for later use, e.g. to allow Agent to read it.
        It seems that when we don't read content bytes inside Proxy filter, the references bytes
        are cleared - probably due to some memory optimization reasons. This is a hacky way to
        preserve message body for later.
         */
        contents.getTextContents();
    }
}
