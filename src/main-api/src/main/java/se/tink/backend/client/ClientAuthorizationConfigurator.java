package se.tink.backend.client;

import com.google.common.base.Charsets;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.Filterable;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.codec.binary.Base64;


public class ClientAuthorizationConfigurator {

    public static ClientAuthorizationConfigurator decorateAndInstantiate(Filterable jerseyResource) {
        return new ClientAuthorizationConfigurator(jerseyResource);
    }

    public class ClientAuthenticationFilter extends ClientFilter {
        @Override
        public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
            if (bearerToken != null) {
                request.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
            } else if (username != null && password != null) {
                String basicAuthorization = basicCredentials(ClientAuthorizationConfigurator.this.username,
                        ClientAuthorizationConfigurator.this.password);

                request.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, "Basic " + basicAuthorization);
            }

            return getNext().handle(request);
        }
    }

    static String basicCredentials(String username, String password) {
        return Base64.encodeBase64String((username + ":" + password).getBytes(Charsets.ISO_8859_1));
    }

    private String bearerToken;
    private String username;
    private String password;

    public ClientAuthorizationConfigurator(Filterable filterable) {
        // Here is would be nice to iterate over all Filters on jerseyResource and assert that no other filter of type
        // ClientAuthenticationFilter has been added. However, I haven't figured out a way to do that...

        filterable.addFilter(new ClientAuthenticationFilter());
    }

    public ClientAuthorizationConfigurator(Client client) {
        // Here is would be nice to iterate over all Filters on jerseyResource and assert that no other filter of type
        // ClientAuthenticationFilter has been added. However, I haven't figured out a way to do that...

        client.addFilter(new ClientAuthenticationFilter());
    }

    public void setBasicAuthorization(String username, String password) {
        this.username = username;
        this.password = password;
        this.bearerToken = null;
    }

    public void setBearerToken(String bearerToken) {
        this.username = null;
        this.password = null;
        this.bearerToken = bearerToken;
    }

}
