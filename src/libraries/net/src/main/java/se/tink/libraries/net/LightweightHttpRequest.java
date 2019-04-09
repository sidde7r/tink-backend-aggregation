package se.tink.libraries.net;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import javax.ws.rs.core.MediaType;
import org.json.JSONObject;

public class LightweightHttpRequest {

    private Builder builder;
    private boolean consumed = false;

    LightweightHttpRequest(Builder builder) {
        if (builder == null) {
            throw new IllegalStateException("Need valid WebResource.Builder");
        }
        this.builder = builder;
    }

    public String getString() {
        ClientResponse response = get();
        if (response != null) {
            String body = response.getEntity(String.class);
            return body;
        }
        return null;
    }

    public ClientResponse get() {
        if (consumed) {
            throw new IllegalStateException("This Http Request has already been consumed");
        }

        consumed = true;

        ClientResponse response = builder.get(ClientResponse.class);
        return response;
    }

    public void post(JSONObject json) {
        if (consumed) {
            throw new IllegalStateException("This Http Request has already been consumed");
        }

        consumed = true;

        builder = builder.type(MediaType.APPLICATION_JSON_TYPE);
        ClientResponse response = null;
        try {
            response = builder.post(ClientResponse.class, json.toString());
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
