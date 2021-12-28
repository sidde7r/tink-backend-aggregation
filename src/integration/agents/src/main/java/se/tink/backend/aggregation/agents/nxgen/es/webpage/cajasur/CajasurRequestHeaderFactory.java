package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;

public class CajasurRequestHeaderFactory {

    public static Map<String, Object> createBasicHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put(
                HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
        headers.put(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate");
        headers.put(HttpHeaders.CONNECTION, "Keep-alive");
        headers.put("Keep-Alive", "300");
        headers.put(HttpHeaders.ACCEPT_LANGUAGE, "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3");
        return headers;
    }
}
