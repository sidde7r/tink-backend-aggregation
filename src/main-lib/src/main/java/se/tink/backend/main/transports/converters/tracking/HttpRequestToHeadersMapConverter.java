package se.tink.backend.main.transports.converters.tracking;

import com.google.common.collect.Maps;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public class HttpRequestToHeadersMapConverter {

    public static Map<String, String> getHeadersAsMap(HttpServletRequest httpRequest) {
        Map<String, String> headers = Maps.newHashMap();
        Enumeration<String> headerNames = httpRequest.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();

            headers.put(headerName, httpRequest.getHeader(headerName));
        }
        return headers;
    }
}
