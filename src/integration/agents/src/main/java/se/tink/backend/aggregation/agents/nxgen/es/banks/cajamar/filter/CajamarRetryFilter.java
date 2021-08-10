package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.filter;

import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.ErrorCodes;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.AbstractRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class CajamarRetryFilter extends AbstractRetryFilter {

    /**
     * @param maxNumRetries Number of additional retries to be performed.
     * @param retrySleepMilliseconds Time im milliseconds that will be spent sleeping between
     */
    public CajamarRetryFilter(int maxNumRetries, long retrySleepMilliseconds) {
        super(maxNumRetries, retrySleepMilliseconds);
    }

    /**
     * Try to retry when bank responses with 403 and body: {"code":"SYS107","message":"Ups, parece
     * que hay un problema con la app. Por favor, sal de la app y vuelve a
     * conectarte.","errorId":null} or code 400 and body {"code" : "SYS203", "message" : "Ups, hemos
     * tenido un problema. Vuelve a intentarlo o contacta con nosotros."}
     *
     * @param response the response to analyze.
     * @return true when is valid
     */
    @Override
    protected boolean shouldRetry(HttpResponse response) {
        String errorBody = response.getBody(String.class);
        return errorBody.contains(ErrorCodes.PLEASE_RECONNECT_APP)
                || errorBody.contains(ErrorCodes.SERVICE_TEMPORARY_UNAVAILABLE);
    }

    @Override
    protected boolean shouldRetry(RuntimeException exception) {
        if (exception instanceof HttpClientException) {
            return TimeoutFilter.isConnectionTimeoutException((HttpClientException) exception);
        }
        return false;
    }
}
