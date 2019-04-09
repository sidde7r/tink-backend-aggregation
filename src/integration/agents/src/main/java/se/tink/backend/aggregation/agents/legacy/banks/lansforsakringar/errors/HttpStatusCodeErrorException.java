package se.tink.backend.aggregation.agents.banks.lansforsakringar.errors;

import com.google.common.base.Objects;
import com.sun.jersey.api.client.ClientResponse;

public class HttpStatusCodeErrorException extends Exception {

    private final ClientResponse response;

    public HttpStatusCodeErrorException(ClientResponse response, String message) {
        super(message);
        this.response = response;
    }

    public ClientResponse getResponse() {
        return response;
    }

    public boolean hasErrorCode(int integer) {
        String errorCode = response.getHeaders().getFirst("Error-Code");

        String stringValue = String.valueOf(integer);
        return Objects.equal(stringValue, errorCode);
    }
}
