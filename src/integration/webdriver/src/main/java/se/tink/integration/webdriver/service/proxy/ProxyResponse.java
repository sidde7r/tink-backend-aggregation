package se.tink.integration.webdriver.service.proxy;

import com.browserup.bup.util.HttpMessageContents;
import com.browserup.bup.util.HttpMessageInfo;
import io.netty.handler.codec.http.HttpResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProxyResponse {

    private final HttpResponse response;
    private final HttpMessageContents contents;
    private final HttpMessageInfo messageInfo;

    public boolean urlContainsLowerCase(String value) {
        String url = messageInfo.getUrl();
        return url.toLowerCase().contains(value.toLowerCase());
    }
}
