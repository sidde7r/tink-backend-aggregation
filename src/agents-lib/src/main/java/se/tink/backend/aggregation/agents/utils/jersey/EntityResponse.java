package se.tink.backend.aggregation.agents.utils.jersey;

import com.sun.jersey.api.client.ClientResponse;

public class EntityResponse {

    private ClientResponse clientResponse;
    private Object entity;

    public EntityResponse(ClientResponse clientResponse, Object entity) {
        this.clientResponse = clientResponse;
        this.entity = entity;
    }

    public ClientResponse getClientResponse() {
        return clientResponse;
    }

    public <T> T getEntity(Class<T> entityClass) {
        if (entityClass == null) {
            return null;
        }
        return entityClass.cast(entity);
    }
}
