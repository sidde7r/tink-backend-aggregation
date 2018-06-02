package se.tink.backend.connector.cli;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.Filterable;
import javax.ws.rs.core.HttpHeaders;

public class ConnectorTokenDecoration {
    public static void set(Filterable filterable, String token) {
        filterable.addFilter(new SetConnectorTokenFilter(token));
    }

    private static class SetConnectorTokenFilter extends ClientFilter {
        private final String token;

        public SetConnectorTokenFilter(String token) {
            this.token = token;
        }

        @Override
        public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
            request.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, "Token " + token);
            return getNext().handle(request);
        }
    }
}
