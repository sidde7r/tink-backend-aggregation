package se.tink.backend.aggregation.agents.creditcards.supremecard.v2;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.model.AccountInfoResponse;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc.CollectBankIdRequest;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc.CompleteBankIdRequest;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc.OrderBankIdRequest;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc.OrderBankIdResponse;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc.SamlRequest;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc.TransactionsResponse;

public class SupremeCardApiAgent {
    private final Client client;

    private SupremeCardApiAgent(Client client) {
        this.client = client;
    }

    static SupremeCardApiAgent createApiAgent(Client client) {
        return new SupremeCardApiAgent(client);
    }

    private WebResource.Builder createRequest(String url, String referer, String accept) {
        return client.resource(url)
                .accept(accept)
                .header("Referer", referer)
                // Supreme card has some check of the user agent, thus we cannot use our default.
                .header("User-Agent", SupremeCardApiConstants.USER_AGENT);
    }

    private WebResource.Builder createPostRequest(
            String url, String referer, String accept, String contentType) {
        return createRequest(url, referer, accept).type(contentType);
    }

    private WebResource.Builder createGetRequest(String url, String referer, String accept) {
        return createRequest(url, referer, accept);
    }

    private <T> T postRequestWithJsonAcceptAndType(
            String url, String referer, Class<T> responseClass, Object requestEntity) {
        return createPostRequest(
                        url, referer, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON)
                .post(responseClass, requestEntity);
    }

    private <T> T postRequestWithOptionalAcceptAndType(
            String url,
            String referer,
            String accept,
            String contentType,
            Class<T> responseClass,
            Object requestEntity) {
        return createPostRequest(url, referer, accept, contentType)
                .post(responseClass, requestEntity);
    }

    private <T> T getRequestWithOptionalAccept(
            String url, String referer, String accept, Class<T> responseClass) {
        return createGetRequest(url, referer, accept).get(responseClass);
    }

    ClientResponse initiateBankId() {
        return getRequestWithOptionalAccept(
                SupremeCardApiConstants.COLLECT_AUTH_CHECKPOINT_URL,
                SupremeCardApiConstants.BASE_URL,
                MediaType.TEXT_HTML,
                ClientResponse.class);
    }

    ClientResponse followInitiateBankIdRedirect(String url) {
        return getRequestWithOptionalAccept(
                url, SupremeCardApiConstants.BASE_URL, MediaType.TEXT_HTML, ClientResponse.class);
    }

    ClientResponse initiateBankIdLogin(String url, String referer) {
        return getRequestWithOptionalAccept(
                url, referer, MediaType.TEXT_HTML, ClientResponse.class);
    }

    OrderBankIdResponse orderBankId(String url, String referer, String ssn) {
        return postRequestWithJsonAcceptAndType(
                url + SupremeCardApiConstants.ORDER_BANKID_PATH,
                referer,
                OrderBankIdResponse.class,
                OrderBankIdRequest.createRequestFromSsn(ssn));
    }

    CollectBankIdResponse collectBankId(String referer, OrderBankIdResponse orderBankIdResponse) {
        return postRequestWithJsonAcceptAndType(
                orderBankIdResponse.getCollectUrl(),
                referer,
                CollectBankIdResponse.class,
                CollectBankIdRequest.createRequestFromOrderBankIdResponse(orderBankIdResponse));
    }

    ClientResponse completeBankId(
            String referer,
            OrderBankIdResponse orderBankIdResponse,
            CollectBankIdResponse collectBankIdResponse) {
        return postRequestWithOptionalAcceptAndType(
                collectBankIdResponse.getCompleteUrl(),
                referer,
                MediaType.WILDCARD,
                MediaType.APPLICATION_JSON,
                ClientResponse.class,
                CompleteBankIdRequest.createRequestFromCollectBankIdResponse(orderBankIdResponse));
    }

    ClientResponse finishBankIdLogin(String url, String referer, SamlRequest samlRequest) {
        return postRequestWithOptionalAcceptAndType(
                url,
                referer,
                MediaType.TEXT_HTML,
                MediaType.APPLICATION_FORM_URLENCODED,
                ClientResponse.class,
                samlRequest);
    }

    ClientResponse followFinishBankIdLoginRedirect(String url, String referer) {
        return getRequestWithOptionalAccept(
                url, referer, MediaType.TEXT_HTML, ClientResponse.class);
    }

    AccountInfoResponse fetchAccountInfo() {
        return createRequest(
                        SupremeCardApiConstants.ACCOUNT_INFO_URL,
                        SupremeCardApiConstants.MY_PAGE_URL,
                        MediaType.APPLICATION_JSON)
                .header(
                        SupremeCardApiConstants.REQUESTED_WITH_HEADER_KEY,
                        SupremeCardApiConstants.REQUESTED_WITH_HEADER_VALUE)
                .post(AccountInfoResponse.class);
    }

    TransactionsResponse fetchTransactions(TransactionsRequest transactionsRequest) {
        return createPostRequest(
                        SupremeCardApiConstants.TRANSCATIONS_URL,
                        SupremeCardApiConstants.MY_PAGE_URL,
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                        SupremeCardApiConstants.REQUESTED_WITH_HEADER_KEY,
                        SupremeCardApiConstants.REQUESTED_WITH_HEADER_VALUE)
                .post(TransactionsResponse.class, transactionsRequest);
    }

    String fetchMyPage() {
        return createGetRequest(
                        SupremeCardApiConstants.MY_PAGE_URL,
                        SupremeCardApiConstants.MY_PAGE_URL,
                        MediaType.TEXT_HTML)
                .get(String.class);
    }
}
