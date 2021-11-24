package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient;

import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BpceResponseHandler extends DefaultResponseStatusHandler {

    private static final String TECHNICAL_ERROR_MSG = "ERREUR Technique";
    private static final String PAGE_ERROR_HEADER = "Page_Erreur";
    private static final String OCTET_STREAM_CONTENT_TYPE = "application/octet-stream";

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (isBankInternalError(httpResponse)
                || isBankTechnicalError(httpResponse)
                || isHtmlErrorPage(httpResponse)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(httpResponse.getBody(String.class));
        } else if (isNoAvailableAccountsError(httpResponse)) {
            throw LoginError.NO_ACCOUNTS.exception();
        }
        super.handleResponse(httpRequest, httpResponse);
    }

    private boolean isNoAvailableAccountsError(HttpResponse response) {
        return response.getStatus() == 404
                && response.getBody(BpceErrorResponse.class).isNoAvailableAccounts();
    }

    private boolean isBankInternalError(HttpResponse response) {
        return response.getStatus() == 500
                && response.getBody(BpceErrorResponse.class).isInternalError();
    }

    private boolean isBankTechnicalError(HttpResponse response) {
        return response.getStatus() == 503
                && response.getBody(String.class).equals(TECHNICAL_ERROR_MSG);
    }

    private boolean isHtmlErrorPage(HttpResponse response) {
        MultivaluedMap<String, String> headers = response.getHeaders();
        String contentTypeValue = headers.getFirst("Content-Type");
        return headers.containsKey(PAGE_ERROR_HEADER)
                && OCTET_STREAM_CONTENT_TYPE.equals(contentTypeValue);
    }
}
