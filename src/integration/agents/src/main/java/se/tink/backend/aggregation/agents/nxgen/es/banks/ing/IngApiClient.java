package se.tink.backend.aggregation.agents.nxgen.es.banks.ing;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc.LoginID;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc.LoginPinPad;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc.LoginPinPositions;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc.LoginTicket;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc.Client;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc.Movements;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc.Product;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class IngApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngApiClient.class);

    private final TinkHttpClient client;

    public IngApiClient(TinkHttpClient client) {
        this.client = client;
    }

    /**
     * This is the final part of the login sequence. The response sets a cookie that is needed for
     * further calls.
     *
     * @param ticket Session ticket received from {@link #putLoginRestSession(LoginPinPositions)}
     * @return true if everything is OK
     */
    public boolean postApiLoginAuthResponse(String ticket) {
        try {
            client.request(IngConstants.Url.GENOMA_API_LOGIN_AUTH_RESPONSE)
                    .queryParam(IngConstants.Query.DEVICE, IngConstants.Default.MOBILE_PHONE)
                    .queryParam(IngConstants.Query.TICKET, ticket)
                    .post();
            return true;
        } catch (HttpResponseException | HttpClientException exception) {
            LOGGER.error("Failed to call postApiLoginAuthResponse", exception);
            return false;
        }
    }

    public boolean deleteApiRestSession() {
        try {
            client.request(IngConstants.Url.GENOMA_API_REST_SESSION).delete();
            return true;
        } catch (HttpResponseException | HttpClientException exception) {
            LOGGER.error("Failed to call deleteApiRestSession", exception);
            return false;
        }
    }

    public LoginTicket putLoginRestSession(LoginPinPositions requestBody) {
        return client.request(IngConstants.Url.GENOMA_LOGIN_REST_SESSION)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(LoginTicket.class, requestBody);
    }

    public Client getApiRestClient() {
        return client.request(IngConstants.Url.GENOMA_API_REST_CLIENT)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .get(Client.class);
    }

    public List<Product> getApiRestProducts() {
        return Arrays.asList(
                client.request(IngConstants.Url.GENOMA_API_REST_PRODUCTS)
                        .type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .get(Product[].class));
    }

    public Movements getApiRestProductsMovements(
            String productUUID, LocalDate fromDate, LocalDate toDate, int offset) {

        return client.request(
                        new URL(IngConstants.Url.GENOMA_API_REST_PRODUCTS_MOVEMENTS)
                                .parameter("product", productUUID))
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam(IngConstants.Query.FROM_DATE, IngUtils.DATE_FORMATTER.format(fromDate))
                .queryParam(IngConstants.Query.TO_DATE, IngUtils.DATE_FORMATTER.format(toDate))
                .queryParam(
                        IngConstants.Query.LIMIT,
                        Integer.toString(IngConstants.FetchControl.PAGE_SIZE))
                .queryParam(IngConstants.Query.OFFSET, Integer.toString(offset))
                .get(Movements.class);
    }

    public boolean getApiRestCommunicationLogoutAllSessions() {
        try {
            this.getApiRestCommunication(
                    IngConstants.Default.SESSION_NAME_ALL,
                    IngConstants.Default.ACTION_NAME_LOGOUT,
                    IngConstants.Default.OPERATION_NAME_EMPTY);
            return true;
        } catch (HttpResponseException | HttpClientException exception) {
            return false;
        }
    }

    String getApiRestCommunication(String sessionName, String actionName, String operationName) {
        return client.request(IngConstants.Url.GENOMA_API_REST_COMMUNICATION)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam(IngConstants.Query.SESSION_NAME, sessionName)
                .queryParam(IngConstants.Query.ACTION_NAME, actionName)
                .queryParam(IngConstants.Query.OPERATION_NAME, operationName)
                .get(String.class);
    }

    public LoginPinPad postLoginRestSession(LoginID requestBody) {
        return client.request(IngConstants.Url.GENOMA_LOGIN_REST_SESSION)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(LoginPinPad.class, requestBody);
    }
}
