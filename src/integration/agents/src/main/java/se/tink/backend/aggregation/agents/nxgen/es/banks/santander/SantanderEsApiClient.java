package se.tink.backend.aggregation.agents.nxgen.es.banks.santander;

import java.time.LocalDate;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.authenticator.rpc.AuthenticateCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.entities.CreditCardRepositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.rpc.CreditCardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.rpc.SantanderEsCreditCardDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.rpc.SantanderEsCreditCardTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.FundEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc.FundDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc.FundDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.rpc.LoanDetailsRequest;
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

    public CreditCardDetailsResponse fetchCreditCardDetails(String userDataXml, String cardNumber) {
        String creditCardDetailsRequest =
                SantanderEsCreditCardDetailsRequest.create(tokenCredential, userDataXml, cardNumber);

        String soapResponseString = postSoapMessage(SantanderEsConstants.Urls.SCH_BAMOBI,
                SantanderEsConstants.Urls.SCH_BAMOBI.toString(),
                creditCardDetailsRequest);

        return SantanderEsXmlUtils.deserializeFromSoapString(soapResponseString,
                SantanderEsConstants.NodeTags.METHOD_RESULT,
                CreditCardDetailsResponse.class);
    }

    public FundDetailsResponse fetchFundDetails(String userDataXml, FundEntity fundEntity) {
        String fundDetailsRequest = FundDetailsRequest.create(tokenCredential, userDataXml, fundEntity);

        String soapResponseString = postSoapMessage(SantanderEsConstants.Urls.SCH_BAMOBI_FONDOS,
                SantanderEsConstants.Urls.SCH_BAMOBI_FONDOS.toString(),
                fundDetailsRequest);

        return SantanderEsXmlUtils.deserializeFromSoapString(soapResponseString,
                SantanderEsConstants.NodeTags.METHOD_RESULT,
                FundDetailsResponse.class);
    }

    public CreditCardTransactionsResponse fetchCreditCardTransactions(String userDataXml, CardEntity card,
            LocalDate fromDate, LocalDate toDate, CreditCardRepositionEntity pagination) {
        String creditCardTransactionsRequest = SantanderEsCreditCardTransactionsRequest
                .create(tokenCredential, userDataXml, card, fromDate, toDate, pagination);

        String soapResponseString = postSoapMessage(SantanderEsConstants.Urls.SCH_BAMOBI,
        SantanderEsConstants.Urls.SCH_BAMOBI.toString(),
        creditCardTransactionsRequest);

        return SantanderEsXmlUtils.deserializeFromSoapString(soapResponseString,
                SantanderEsConstants.NodeTags.METHOD_RESULT,
                CreditCardTransactionsResponse.class);
    }

    public String fetchLoanDetails(String userDataXml, LoanEntity loanEntity) {
        String loanDetailsRequest = LoanDetailsRequest.create(tokenCredential, userDataXml, loanEntity);

        String soapResponseString = postSoapMessage(SantanderEsConstants.Urls.SCH_BAMOBI_PRESTAMOS,
                SantanderEsConstants.Urls.SCH_BAMOBI_PRESTAMOS.toString(),
                loanDetailsRequest);

        return soapResponseString;
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
