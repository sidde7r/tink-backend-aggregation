package se.tink.integration.webdriver;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.client.ClientUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openqa.selenium.Proxy;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProxyInitializer {

    public static BrowserUpProxy startProxyServer() {
        BrowserUpProxy proxy = new BrowserUpProxyServer();
        proxy.setTrustAllServers(true);
        proxy.start(0);
        return proxy;
    }

    public static Proxy toSeleniumProxy(BrowserUpProxy browserUpProxy) {
        Proxy proxy = ClientUtil.createSeleniumProxy(browserUpProxy);

        // fix proxy host name (https://groups.google.com/g/browsermob-proxy/c/fALhlU0p0pI)
        proxy.setHttpProxy("localhost:" + browserUpProxy.getPort());
        proxy.setSslProxy("localhost:" + browserUpProxy.getPort());

        return proxy;
    }

    public static void shutDownProxy(BrowserUpProxy browserUpProxy) {
        browserUpProxy.abort();
    }
}
