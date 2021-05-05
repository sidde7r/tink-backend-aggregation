package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import java.util.Objects;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public abstract class HttpResponseExceptionRequestRepeater<T> {

    private final int maxNumberOfRepetitions;

    public HttpResponseExceptionRequestRepeater(final int maxNumberOfRepetitions) {
        this.maxNumberOfRepetitions = maxNumberOfRepetitions;
    }

    public T execute() {
        HttpResponseException httpResponseExceptionToRethrow = null;
        for (int i = 0; i < maxNumberOfRepetitions; i++) {
            try {
                return request();
            } catch (HttpResponseException ex) {
                if (!checkIfRepeat(ex)) {
                    throw ex;
                }
                httpResponseExceptionToRethrow = ex;
            }
        }
        throw Objects.requireNonNull(httpResponseExceptionToRethrow);
    }

    public abstract T request();

    public abstract boolean checkIfRepeat(HttpResponseException ex);
}
