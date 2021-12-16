package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.filter;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.rpc.LaCaixaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@AllArgsConstructor
public class LaCaixaSessionExpiredFilter extends Filter {

    private final PersistentStorage storage;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        if (response.getStatus() == 409) {
            LaCaixaErrorResponse body = response.getBody(LaCaixaErrorResponse.class);
            if (isCanNotPerformOperation(body)
                    || isAccessToCaixaBankNowNotPossible(body)
                    || isCaixabankSignSignatureMechanismHasBeenBlockedResponse(body)) {
                storage.clear();
                throw SessionError.SESSION_EXPIRED.exception();
            }
        }
        return response;
    }

    private boolean isCanNotPerformOperation(LaCaixaErrorResponse errorResponse) {
        // original error message: "Para realizar esta operaci?n debe tener un usuario con modalidad
        // operativa. Contacte con su oficina para contratarla"
        return "2968".equals(errorResponse.getCode());
    }

    private boolean isAccessToCaixaBankNowNotPossible(LaCaixaErrorResponse errorResponse) {
        // original error message: "Por\nmotivos de seguridad, no ha sido posible el acceso a
        // CaixaBankNow. Por\nfavor, vuelve a intentarlo o ponte en contacto con nosotros a través
        // del\nservicio de atención al cliente."
        return "-1".equals(errorResponse.getCode())
                && errorResponse.getMessage().contains("no ha sido posible el acceso");
    }

    private boolean isCaixabankSignSignatureMechanismHasBeenBlockedResponse(
            LaCaixaErrorResponse errorResponse) {
        // original error message: "Por motivos de seguridad se ha bloqueado su mecanismo de firma
        // Caixabank Sign. Por favor, p?ngase en contacto con su oficina"
        return "3735".equals(errorResponse.getCode());
    }
}
