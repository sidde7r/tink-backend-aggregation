package se.tink.backend.aggregation.agents.brokers.lysa;

import com.google.common.base.Preconditions;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import se.tink.backend.aggregation.agents.brokers.lysa.model.TransactionEntity;
import se.tink.backend.aggregation.agents.brokers.lysa.rpc.PollBankIdResponse;
import se.tink.backend.aggregation.agents.brokers.lysa.rpc.StartBankIdRequest;
import se.tink.backend.aggregation.agents.brokers.lysa.rpc.StartBankIdResponse;
import se.tink.libraries.social.security.SocialSecurityNumber;
import javax.ws.rs.core.MediaType;
import java.util.List;

public class LysaClient {
    private static final String BASE_URL = "https://api.lysa.se/";

    private final Client httpClient;
    private final String aggregator;

    public LysaClient(Client httpClient, String aggregator) {
        this.aggregator = aggregator;
        this.httpClient = httpClient;
    }

    public StartBankIdResponse startBankId(String identificationNumber) {
        Preconditions.checkNotNull(identificationNumber);

        SocialSecurityNumber.Sweden socialSecurityNumber = new SocialSecurityNumber.Sweden(identificationNumber);
        Preconditions.checkState(socialSecurityNumber.isValid());

        StartBankIdRequest request = new StartBankIdRequest();
        request.setIdentificationNumber(socialSecurityNumber.asString());

        return createClientRequest("login/bankid").post(StartBankIdResponse.class, request);
    }

    public PollBankIdResponse pollBankId(String transactionId) {
        Preconditions.checkNotNull(transactionId);

        return createClientRequest("login/bankid/" + transactionId).get(PollBankIdResponse.class);
    }

    public List<TransactionEntity> getTransactions() {
        return createClientRequest("transactions").get(new GenericType<List<TransactionEntity>>() {});
    }

    private WebResource.Builder createClientRequest(String uri) {
        Preconditions.checkNotNull(uri);

        WebResource.Builder builder = httpClient.resource(BASE_URL + uri)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header("User-Agent", aggregator);

        return builder;
    }

    public void logout() {
        createClientRequest("logout").delete();
    }
}
