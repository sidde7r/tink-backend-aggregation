package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.PathVariables;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.XmlConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.IbanAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.PsuDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.InitAuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.InitAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ScaStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.SelectAuthenticationMethodRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.SelectAuthenticationMethodResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.configuration.SparkassenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchTransactionsResponseWrapper;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class SparkassenApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private final String bankCode;
    private SparkassenConfiguration configuration;

    public SparkassenApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            Credentials credentials,
            String bankCode) {

        this.client = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.bankCode = bankCode;
    }

    private SparkassenConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(SparkassenConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {

        if (url.get().contains("{" + PathVariables.BANK_CODE + "}")) {
            url = url.parameter(PathVariables.BANK_CODE, bankCode);
        }

        url.parameter(PathVariables.BANK_CODE, bankCode);

        return client.request(url.parameter(PathVariables.BANK_CODE, bankCode))
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID());
    }

    private RequestBuilder createRequestInSession(URL url) {

        return createRequest(url)
                .header(
                        HeaderKeys.AUTHORIZATION,
                        persistentStorage.get(StorageKeys.AUTHORIZATION_ID))
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID));
    }

    public ConsentResponse createConsent() {

        List<AccountsEntity> ibans =
                Stream.of(credentials.getField(CredentialKeys.IBAN).split(","))
                        .map(String::trim)
                        .map(AccountsEntity::new)
                        .collect(Collectors.toList());

        ConsentRequest getConsentRequest =
                new ConsentRequest(
                        // If AllAccountsAccessEntity is used with a value of "allAccounts",
                        // IBANs are not needed but the generated consent can only be used
                        // for fetching a list of accounts
                        new IbanAccessEntity(ibans, ibans),
                        true,
                        LocalDate.now().plusDays(90).toString(),
                        FormValues.FREQUENCY_PER_DAY,
                        false);

        return createRequest(Urls.GET_CONSENT)
                .header(HeaderKeys.TPP_REDIRECT_PREFERRED, false)
                .post(ConsentResponse.class, getConsentRequest);
    }

    public InitAuthorizationResponse initializeAuthorization(
            URL url, String username, String password) {
        return createRequest(url)
                .header(HeaderKeys.PSU_ID, username)
                .post(
                        InitAuthorizationResponse.class,
                        new InitAuthorizationRequest(new PsuDataEntity(password)));
    }

    public SelectAuthenticationMethodResponse updateAuthorisationForScaMethod(String methodId) {
        return createRequest(
                        Urls.UPDATE_SCA_METHOD
                                .parameter(
                                        PathVariables.CONSENT_ID,
                                        persistentStorage.get(StorageKeys.CONSENT_ID))
                                .parameter(
                                        PathVariables.AUTHORIZATION_ID,
                                        persistentStorage.get(StorageKeys.AUTHORIZATION_ID)))
                .put(
                        SelectAuthenticationMethodResponse.class,
                        new SelectAuthenticationMethodRequest(methodId));
    }

    public ScaStatusResponse getAuthorisation(URL url) {
        return createRequest(url).get(ScaStatusResponse.class);
    }

    public FinalizeAuthorizationResponse finalizeAuthorization(URL url, String otp) {
        return createRequest(url)
                .put(FinalizeAuthorizationResponse.class, new FinalizeAuthorizationRequest(otp));
    }

    public FetchAccountsResponse fetchAccounts() {
        return createRequestInSession(SparkassenConstants.Urls.FETCH_ACCOUNTS)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .get(FetchAccountsResponse.class);
    }

    public FetchBalancesResponse getAccountBalance(String accountId) {
        return createRequestInSession(
                        Urls.FETCH_BALANCES.parameter(PathVariables.ACCOUNT_ID, accountId))
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .get(FetchBalancesResponse.class);
    }

    public FetchTransactionsResponseWrapper fetchTransactions(String accountId) {
        String xmlResponse =
                createRequestInSession(
                                Urls.FETCH_TRANSACTIONS
                                        .parameter(PathVariables.ACCOUNT_ID, accountId)
                                        .queryParam(
                                                QueryKeys.DATE_FROM,
                                                LocalDate.now().plusDays(90).toString())
                                        .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH))
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_XML)
                        .get(String.class);

        // Fix response for parser
        xmlResponse = fixXmlResponse(xmlResponse);

        try {
            JAXBContext context = JAXBContext.newInstance(FetchTransactionsResponseWrapper.class);
            Unmarshaller m = context.createUnmarshaller();
            return (FetchTransactionsResponseWrapper) m.unmarshal(new StringReader(xmlResponse));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String fixXmlResponse(String xmlResponse) {
        xmlResponse =
                XmlConstants.DOCUMENTS_OPEN
                        + xmlResponse
                                .replace(XmlConstants.VERSION_TAG, "")
                                .replace(XmlConstants.XMLNS_ATRIBUTE, "")
                                .replace(XmlConstants.XMLNS_ATRIBUTE_EXTENDED, "")
                        + XmlConstants.DOCUMENTS_CLOSED;
        return xmlResponse;
    }
}
