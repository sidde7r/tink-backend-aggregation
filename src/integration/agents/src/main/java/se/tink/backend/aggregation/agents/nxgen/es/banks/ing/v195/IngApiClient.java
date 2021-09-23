package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.Query;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.BasicResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.ClientResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.CommunicationsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.CreateSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.CreateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.DeviceDataRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.DismissScaRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.InsecureMobileLoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.PutRestSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.PutSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.ScaStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.TicketResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.IngProduct;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.rpc.MovementsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.rpc.ProductsResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class IngApiClient {

    private final TinkHttpClient client;
    private ProductsResponse cachedProductsResponse;

    public IngApiClient(TinkHttpClient client) {
        this.client = client;
        Filter serviceUnavailable = new ServiceUnavailableBankServiceErrorFilter();
        client.addFilter(serviceUnavailable);
    }

    public RequestBuilder requestJson(String url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    public CreateSessionResponse postLoginRestSession(CreateSessionRequest request) {
        return requestJson(IngConstants.Url.LOGIN_REST_SESSION)
                .post(CreateSessionResponse.class, request);
    }

    public PutRestSessionResponse putLoginRestSession(
            List<Integer> pinPositions, String processId) {
        return requestJson(IngConstants.Url.LOGIN_REST_SESSION)
                .put(
                        PutRestSessionResponse.class,
                        PutSessionRequest.create(pinPositions, processId));
    }

    public ScaStatusResponse getScaStatus(String processId, boolean isLogin) {
        return client.request(Url.LOGIN_SCA_STATUS)
                .queryParam(Query.SEC_PROCESS_ID, processId)
                .queryParam(Query.IS_LOGIN, isLogin ? Query.TRUE : Query.FALSE)
                .get(ScaStatusResponse.class);
    }

    public PutRestSessionResponse putLoginRestSession(String otp, String processId) {
        return requestJson(IngConstants.Url.LOGIN_REST_SESSION)
                .put(PutRestSessionResponse.class, PutSessionRequest.create(otp, processId));
    }

    public boolean postLoginAuthResponse(String ticket, String deviceAction) {

        client.request(IngConstants.Url.LOGIN_AUTH_RESPONSE)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(
                        Form.builder()
                                .put(IngConstants.Form.TICKET, ticket)
                                .put(IngConstants.Form.DEVICE, deviceAction)
                                .build()
                                .serialize());

        return true;
    }

    public ClientResponse getApiRestClient() {
        return requestJson(IngConstants.Url.API_REST_CLIENT).get(ClientResponse.class);
    }

    // Product list
    public ProductsResponse getApiRestProducts() {
        return Optional.ofNullable(this.cachedProductsResponse)
                .orElseGet(
                        () ->
                                ProductsResponse.create(
                                        Arrays.asList(
                                                client.request(Url.API_REST_PRODUCTS)
                                                        .type(MediaType.APPLICATION_JSON)
                                                        .accept(MediaType.APPLICATION_JSON)
                                                        .get(IngProduct[].class))));
    }

    // "Movements" (Transactions)
    public MovementsResponse getApiRestProductMovements(
            String productUuid, LocalDate fromDate, LocalDate toDate, int offset) {

        URL movementsUrl =
                new URL(IngConstants.Url.API_REST_PRODUCTS_MOVEMENTS)
                        .parameter("product", productUuid);

        return requestJson(movementsUrl.get())
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
        return requestJson(IngConstants.Url.API_REST_COMMUNICATION)
                .queryParams(IngConstants.LOGOUT_QUERY)
                .get(CommunicationsResponse.class);
    }

    public String deleteApiRestSession() {
        return requestJson(IngConstants.Url.API_REST_SESSION).delete(String.class);
    }

    public BasicResponse registerInsecureMobileError(
            InsecureMobileLoginError insecureMobileLoginError) {
        return requestJson(Url.REGISTER_MOBILE_ERROR)
                .post(BasicResponse.class, insecureMobileLoginError);
    }

    public BasicResponse dismissSca(DismissScaRequest insecureMobileLoginError) {
        return requestJson(Url.INSERT_ROOTED_MOBILE_DATA)
                .post(BasicResponse.class, insecureMobileLoginError);
    }

    public TicketResponse requestSsoTicket(DeviceDataRequest deviceDataRequest) {
        return requestJson(Url.SSO_TICKET).post(TicketResponse.class, deviceDataRequest);
    }
}
