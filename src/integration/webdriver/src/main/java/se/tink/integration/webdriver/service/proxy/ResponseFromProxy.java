package se.tink.integration.webdriver.service.proxy;

import com.browserup.bup.util.HttpMessageContents;
import com.browserup.bup.util.HttpMessageInfo;
import io.netty.handler.codec.http.HttpResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseFromProxy {

    private final HttpResponse response;
    private final HttpMessageContents contents;
    private final HttpMessageInfo messageInfo;
}
