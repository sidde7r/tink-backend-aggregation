package se.tink.libraries.http.utils;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;

public class HttpResponseHelper {
    private final Logger log;

    public HttpResponseHelper(Logger log) {
        this.log = log;
    }

    public static Response ok() {
        return Response.ok().build();
    }

    public static void error(final Status responseCode) {
        WebApplicationErrorUtils.error(responseCode);
    }

    public void error(final Status responseCode, String logMessage) {
        WebApplicationErrorUtils.error(log, responseCode, logMessage);
    }

    public void error(final Status responseCode, String logMessage, Throwable e) {
        WebApplicationErrorUtils.error(log, responseCode, logMessage, e);
    }
}
