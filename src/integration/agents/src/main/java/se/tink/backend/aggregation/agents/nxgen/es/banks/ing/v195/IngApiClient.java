package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.ClientResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.CommunicationsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.CreateSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.CreateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.PutRestSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.PutSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.Product;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.rpc.MovementsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.rpc.ProductsResponse;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class IngApiClient {

    private final TinkHttpClient client;

    private ProductsResponse cachedProductsResponse;

    public IngApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public CreateSessionResponse postLoginRestSession(
            String username, int usernameType, String dob, String deviceId) {

        LocalDate birthday = LocalDate.parse(dob, IngUtils.BIRTHDAY_INPUT);
        CreateSessionRequest request =
                CreateSessionRequest.create(username, usernameType, birthday, deviceId);

        return client.request(IngConstants.Url.LOGIN_REST_SESSION)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(CreateSessionResponse.class, request);
    }

    public PutRestSessionResponse putLoginRestSession(
            List<Integer> pinPositions, String processId) {
        return client.request(IngConstants.Url.LOGIN_REST_SESSION)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(
                        PutRestSessionResponse.class,
                        PutSessionRequest.create(pinPositions, processId));
    }

    public boolean postLoginAuthResponse(String ticket) {

        client.request(IngConstants.Url.LOGIN_AUTH_RESPONSE)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(
                        Form.builder()
                                .put(IngConstants.Form.TICKET, ticket)
                                .put(IngConstants.Form.DEVICE, IngConstants.Default.MOBILE_PHONE)
                                .build()
                                .serialize());

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
        if (cachedProductsResponse == null) {
            Product[] products =
                    client.request(IngConstants.Url.API_REST_PRODUCTS)
                            .type(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .get(Product[].class);

            List<Product> productList = Arrays.asList(products);

            IngUtils.logUnknownProducts(productList);

            cachedProductsResponse = ProductsResponse.create(productList);
        }

        return cachedProductsResponse;
    }

    // "Movements" (Transactions)
    public MovementsResponse getApiRestProductMovements(
            String productUuid, LocalDate fromDate, LocalDate toDate, int offset) {

        URL movementsUrl =
                new URL(IngConstants.Url.API_REST_PRODUCTS_MOVEMENTS)
                        .parameter("product", productUuid);

        return client.request(movementsUrl)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam(IngConstants.Query.FROM_DATE, IngUtils.DATE_FORMATTER.format(fromDate))
                .queryParam(IngConstants.Query.TO_DATE, IngUtils.DATE_FORMATTER.format(toDate))
                .queryParam(
                        IngConstants.Query.LIMIT,
                        Integer.toString(IngConstants.FetchControl.PAGE_SIZE))
                .queryParam(IngConstants.Query.OFFSET, Integer.toString(offset))
                .get(MovementsResponse.class);
    }

    // This request is performed by the mobile app prior to the delete session request. It's unclear
    // whether this
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
