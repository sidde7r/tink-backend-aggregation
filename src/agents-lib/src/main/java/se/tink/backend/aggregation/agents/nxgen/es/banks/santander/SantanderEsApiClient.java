package se.tink.backend.aggregation.agents.nxgen.es.banks.santander;

import java.time.LocalDate;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.authenticator.rpc.AuthenticateCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.rpc.SantanderEsCreditCardDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.rpc.SantanderEsCreditCardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities.RepositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.rpc.FirstPageOfTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.rpc.TransactionPaginationRequest;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SantanderEsApiClient {
    private final TinkHttpClient client;
    private String tokenCredential;

    public SantanderEsApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public void setTokenCredential(String tokenCredential) {
        this.tokenCredential = tokenCredential;
    }

    public String authenticateCredentials(String username, String password) {
        String requestBody = AuthenticateCredentialsRequest.create(username, password);

        return postSoapMessage(SantanderEsConstants.Urls.SANMOV,
                SantanderEsConstants.Urls.SANMOV.toString(),
                requestBody);
    }

    public String login() {
        String requestBody = LoginRequest.create(tokenCredential);

        return postSoapMessage(SantanderEsConstants.Urls.SCH_BAMOBI,
                SantanderEsConstants.Urls.SCH_BAMOBI.toString(),
                requestBody);
    }

    public String fetchTransactions(String userDataXmlString, String contractIdXmlString,
            String balanceXmlString, RepositionEntity repositionEntity) {

        String requestBody = getTransactionsRequestBody(userDataXmlString, contractIdXmlString,
                balanceXmlString, repositionEntity);

        String soapResponseString = postSoapMessage(SantanderEsConstants.Urls.SCH_BAMOBI,
                SantanderEsConstants.Urls.SCH_BAMOBI.toString(),
                requestBody);

        return SerializationUtils.serializeToString(
                SantanderEsXmlUtils.getTagNodeFromSoapString(
                        soapResponseString, SantanderEsConstants.NodeTags.METHOD_RESULT)
        );
    }

    public String fetchCreditCardDetails(String userDataXml, String cardNumber) {
        String creditCardDetailsRequest =
                SantanderEsCreditCardDetailsRequest.create(tokenCredential, userDataXml, cardNumber);

        return postSoapMessage(SantanderEsConstants.Urls.SCH_BAMOBI,
                SantanderEsConstants.Urls.SCH_BAMOBI.toString(),
                creditCardDetailsRequest);
    }

    public String fetchCreditCardTransactions(String userDataXml, CardEntity card,
            LocalDate fromDate, LocalDate toDate, boolean pagination) {
        String creditCardTransactionsRequest = SantanderEsCreditCardTransactionsRequest
                .create(tokenCredential, userDataXml, card.getGeneralInfo().getContractId(),
                        card.getCardNumber(), fromDate, toDate, pagination);

        return postSoapMessage(SantanderEsConstants.Urls.SCH_BAMOBI,
                SantanderEsConstants.Urls.SCH_BAMOBI.toString(),
                creditCardTransactionsRequest);
    }

    private String postSoapMessage(URL url, String soapAction, String body) {
        return client.request(url)
                .header(SantanderEsConstants.Headers.SOAP_ACTION, soapAction)
                .type(SantanderEsConstants.Headers.TEXT_XML_UTF8)
                .accept(MediaType.WILDCARD)
                .post(String.class, body);
    }

    private String getTransactionsRequestBody(String userDataXmlString, String contractIdXmlString,
            String balanceXmlString, RepositionEntity repositionEntity) {
        if (repositionEntity == null) {
            return FirstPageOfTransactionsRequest.create(
                    tokenCredential, userDataXmlString, contractIdXmlString, balanceXmlString, false);
        }

        String repositionXmlString = SantanderEsXmlUtils.parseJsonToXmlString(repositionEntity);
        return TransactionPaginationRequest.create(tokenCredential, userDataXmlString, contractIdXmlString,
                balanceXmlString, true, repositionXmlString);
    }
}
