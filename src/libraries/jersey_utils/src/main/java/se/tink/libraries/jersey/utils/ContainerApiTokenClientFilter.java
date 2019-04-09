package se.tink.libraries.jersey.utils;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.Filterable;
import javax.ws.rs.core.HttpHeaders;

public class ContainerApiTokenClientFilter extends ClientFilter {

    private final String authorizationHeaderValue;

    private ContainerApiTokenClientFilter(String token) {
        // TODO: Make "token" a shared constant between this class and
        // ContainerAuthorizationResourceFilterFactory. Where?
        this.authorizationHeaderValue = "token " + token;
    }

    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        request.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);
        return getNext().handle(request);
    }

    public static void decorate(Filterable jerseyResource, String token) {
        jerseyResource.addFilter(new ContainerApiTokenClientFilter(token));
    }
}
