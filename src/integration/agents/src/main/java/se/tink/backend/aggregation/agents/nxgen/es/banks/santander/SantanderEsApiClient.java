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
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.FundEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.PortfolioRepositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.StockEmissionCode;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc.FundDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc.FundDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc.InstrumentDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc.InstrumentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc.PortfolioDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc.PortfolioDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities.RepositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.rpc.FirstPageOfTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.rpc.TransactionPaginationRequest;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
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

        return postSoapMessage(SantanderEsConstants.Urls.AUTHENTICATION_ENDPOINT,
                SantanderEsConstants.Urls.AUTHENTICATION_ENDPOINT.toString(),
                requestBody);
    }

    public String login() {
        String requestBody = LoginRequest.create(tokenCredential);

        return postSoapMessage(SantanderEsConstants.Urls.WEB_SERVICE_ENDPOINT,
                SantanderEsConstants.Urls.WEB_SERVICE_ENDPOINT.toString(),
                requestBody);
    }

    public String fetchTransactions(String userDataXmlString, String contractIdXmlString,
            String balanceXmlString, RepositionEntity repositionEntity) {

        String requestBody = getTransactionsRequestBody(userDataXmlString, contractIdXmlString,
                balanceXmlString, repositionEntity);

        String soapResponseString = postSoapMessage(SantanderEsConstants.Urls.WEB_SERVICE_ENDPOINT,
                SantanderEsConstants.Urls.WEB_SERVICE_ENDPOINT.toString(),
                requestBody);

        return SerializationUtils.serializeToString(
                SantanderEsXmlUtils.getTagNodeFromSoapString(
                        soapResponseString, SantanderEsConstants.NodeTags.METHOD_RESULT)
        );
    }

    public CreditCardDetailsResponse fetchCreditCardDetails(String userDataXml, String cardNumber) {
        String creditCardDetailsRequest =
                SantanderEsCreditCardDetailsRequest.create(tokenCredential, userDataXml, cardNumber);

        String soapResponseString = postSoapMessage(SantanderEsConstants.Urls.WEB_SERVICE_ENDPOINT,
                SantanderEsConstants.Urls.WEB_SERVICE_ENDPOINT.toString(),
                creditCardDetailsRequest);

        return SantanderEsXmlUtils.deserializeFromSoapString(soapResponseString,
                SantanderEsConstants.NodeTags.METHOD_RESULT,
                CreditCardDetailsResponse.class);
    }

    public FundDetailsResponse fetchFundDetails(String userDataXml, FundEntity fundEntity) {
        String fundDetailsRequest = FundDetailsRequest.create(tokenCredential, userDataXml, fundEntity);

        String soapResponseString = postSoapMessage(SantanderEsConstants.Urls.FUNDS_ENDPOINT,
                SantanderEsConstants.Urls.FUNDS_ENDPOINT.toString(),
                fundDetailsRequest);

        return SantanderEsXmlUtils.deserializeFromSoapString(soapResponseString,
                SantanderEsConstants.NodeTags.METHOD_RESULT,
                FundDetailsResponse.class);
    }

    public CreditCardTransactionsResponse fetchCreditCardTransactions(String userDataXml, CardEntity card,
            LocalDate fromDate, LocalDate toDate, CreditCardRepositionEntity pagination) {
        String creditCardTransactionsRequest = SantanderEsCreditCardTransactionsRequest
                .create(tokenCredential, userDataXml, card, fromDate, toDate, pagination);

        String soapResponseString = postSoapMessage(SantanderEsConstants.Urls.WEB_SERVICE_ENDPOINT,
                SantanderEsConstants.Urls.WEB_SERVICE_ENDPOINT.toString(),
                creditCardTransactionsRequest);

        return SantanderEsXmlUtils.deserializeFromSoapString(soapResponseString,
                SantanderEsConstants.NodeTags.METHOD_RESULT,
                CreditCardTransactionsResponse.class);
    }

    public String fetchLoanDetails(String userDataXml, LoanEntity loanEntity) {
        String loanDetailsRequest = LoanDetailsRequest.create(tokenCredential, userDataXml, loanEntity);

        String soapResponseString = postSoapMessage(SantanderEsConstants.Urls.LOANS_ENDPOINT,
                SantanderEsConstants.Urls.LOANS_ENDPOINT.toString(),
                loanDetailsRequest);

        return soapResponseString;
    }

    public PortfolioDetailsResponse fetchPortfolioDetails(String userDataXml, PortfolioEntity portfolio,
            boolean firstPage, PortfolioRepositionEntity paginationData) {

        String portfolioDetailsRequest = PortfolioDetailsRequest
                .create(tokenCredential, userDataXml, portfolio, firstPage, paginationData);

        String soapResponseString = postSoapMessage(SantanderEsConstants.Urls.STOCKS_ENDPOINT,
                SantanderEsConstants.Urls.STOCKS_ENDPOINT.toString(),
                portfolioDetailsRequest);

        return SantanderEsXmlUtils.deserializeFromSoapString(soapResponseString,
                SantanderEsConstants.NodeTags.METHOD_RESULT,
                PortfolioDetailsResponse.class);
    }

    public InstrumentDetailsResponse fetchInstrumentDetails(String userDataXml, StockEmissionCode emissionCode) {
        String instrumentDetailsRequest = InstrumentDetailsRequest.create(tokenCredential, userDataXml, emissionCode);

        String soapResponseString = postSoapMessage(SantanderEsConstants.Urls.STOCKS_ENDPOINT,
                SantanderEsConstants.Urls.STOCKS_ENDPOINT.toString(),
                instrumentDetailsRequest);

        return SantanderEsXmlUtils.deserializeFromSoapString(soapResponseString,
                SantanderEsConstants.NodeTags.METHOD_RESULT,
                InstrumentDetailsResponse.class);
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
