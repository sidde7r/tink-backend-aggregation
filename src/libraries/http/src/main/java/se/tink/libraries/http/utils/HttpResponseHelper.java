package se.tink.libraries.http.utils;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import se.tink.libraries.log.LogUtils;

public class HttpResponseHelper {
    private final LogUtils log;

    public HttpResponseHelper(LogUtils log) {
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
