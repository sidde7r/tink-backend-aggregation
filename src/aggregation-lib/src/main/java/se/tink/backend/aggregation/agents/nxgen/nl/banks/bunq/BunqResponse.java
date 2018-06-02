package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = BunqHashMapDeserializer.BunqDeserializer.class)
public class BunqResponse<T> {
    private T body;

    public T getResponseBody() {
        return body;
    }

    public void setResponseBody(T body) {
        this.body = body;
    }
}
