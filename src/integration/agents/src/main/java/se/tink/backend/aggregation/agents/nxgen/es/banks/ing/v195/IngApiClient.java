package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195;

import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.request.CreateSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.request.PutSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.response.CommunicationsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.response.CreateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.response.PutRestSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.responses.ProductsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.responses.entities.Product;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.rpc.responses.ClientResponse;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class IngApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;

    public IngApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public CreateSessionResponse postLoginRestSession(String username, int usernameType, String dob) {

        LocalDate birthday = LocalDate.parse(dob, IngUtils.BIRTHDAY_INPUT);
        CreateSessionRequest request = CreateSessionRequest.create(username, usernameType, birthday);

        return client.request(IngConstants.Url.LOGIN_REST_SESSION)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(CreateSessionResponse.class, request);
    }

    public PutRestSessionResponse putLoginRestSession(List<Integer> pinPositions) {
        return client.request(IngConstants.Url.LOGIN_REST_SESSION)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(PutRestSessionResponse.class, PutSessionRequest.create(pinPositions));
    }


    public boolean postLoginAuthResponse(String ticket) {

        client.request(IngConstants.Url.LOGIN_AUTH_RESPONSE)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(Form.builder()
                        .put(IngConstants.Form.TICKET, ticket)
                        .put(IngConstants.Form.DEVICE, IngConstants.Default.MOBILE_PHONE)
                        .build().serialize()
                );

        return true;
    }

    public ClientResponse getApiRestClient() {
        return client.request(IngConstants.Url.API_REST_CLIENT)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
    }

    // Product list
    public ProductsResponse getApiRestProducts() {
        Product[] products = client.request(IngConstants.Url.API_REST_PRODUCTS)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(Product[].class);

        List<Product> productList = Arrays.asList(products);

        IngUtils.logUnknownProducts(productList);

        ProductsResponse productsResponse = ProductsResponse.create(productList);

        this.sessionStorage.put(IngConstants.Tags.PRODUCT_LIST, productsResponse);

        return productsResponse;
    }

    // This request is performed by the mobile app prior to the delete session request. It's unclear whether this
    // actually serves any purpose.
    public CommunicationsResponse getApiRestCommunicationLogoutRequest() {
        return client.request(IngConstants.Url.API_REST_COMMUNICATION)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .queryParams(IngConstants.LOGOUT_QUERY)
                .get(CommunicationsResponse.class);
    }

    public String deleteApiRestSession() {
        return client.request(IngConstants.Url.API_REST_SESSION)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .delete(String.class);
    }
}
