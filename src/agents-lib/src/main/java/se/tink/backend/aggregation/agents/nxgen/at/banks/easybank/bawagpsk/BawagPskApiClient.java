package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk;

import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountInformationListResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountStatementItemsResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.LogoutResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.ServiceRequest;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.ServiceResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.Provider;

public class BawagPskApiClient {

    private final TinkHttpClient client;
    private final SessionStorage storage;
    private final String baseUrl;
    private final String bankName;

    private LoginResponse loginResponse; // Saving it here to be processed by the fetchers later

    public BawagPskApiClient(TinkHttpClient client, SessionStorage storage, Provider provider) {
        this.client = client;
        this.storage = storage;

        String[] providerStrings = provider.getPayload().split(",");
        this.baseUrl = providerStrings[0];
        this.bankName = providerStrings[1].trim();
    }

    private RequestBuilder getRequest() {
        return client.request(getUrl())
                .header(HttpHeaders.HOST, this.baseUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML_TYPE)
                .header(HttpHeaders.ACCEPT, BawagPskConstants.Header.ACCEPT)
                .header(HttpHeaders.USER_AGENT, BawagPskConstants.Header.USER_AGENT)
                .header(HttpHeaders.ACCEPT_LANGUAGE, BawagPskConstants.Header.ACCEPT_LANGUAGE);
    }

    private URL getUrl() {
        return new URL(
                String.format("https://%s%s", this.baseUrl, BawagPskConstants.URLS.SERVICE_ENDPOINT));
    }

    public String getBankName(){
        return this.bankName;
    }

    public LoginResponse login(final String requestString) throws HttpResponseException {
        final RequestBuilder requestBuilder = getRequest();
        final LoginResponse response = new LoginResponse(requestBuilder.post(Envelope.class, requestString));
        if (response.requestWasSuccessful()) {
            storage.put(BawagPskConstants.Storage.SERVER_SESSION_ID.name(), response.getServerSessionID());
            storage.put(BawagPskConstants.Storage.QID.name(), response.getQid());
            loginResponse = response;
        }
        return response;
    }

    public Optional<LoginResponse> getLoginResponse() {
        return Optional.ofNullable(loginResponse);
    }

    public ServiceResponse checkIfSessionAlive() {
        // Using this message for the sole purpose of checking if session is alive
        final ServiceRequest request = new ServiceRequest(
                storage.get(BawagPskConstants.Storage.SERVER_SESSION_ID.name()));

        try {
            return new ServiceResponse(getRequest().post(Envelope.class, request.getXml()));
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to marshal JAXB ", e);
        }
    }

    public Optional<String> getFromStorage(final String key) {
        return Optional.ofNullable(storage.get(key));
    }

    public LogoutResponse logout(final String requestString) {
        final Envelope response = getRequest()
                .post(Envelope.class, requestString);
        storage.clear();
        return new LogoutResponse(response);
    }

    public GetAccountInformationListResponse getGetAccountInformationListResponse(final String requestString) {
        final Envelope response = getRequest()
                .post(Envelope.class, requestString);
        return new GetAccountInformationListResponse(response);
    }

    public GetAccountStatementItemsResponse getGetAccountStatementItemsResponse(final String requestString) {
        final Envelope response = getRequest()
                .post(Envelope.class, requestString);
        return new GetAccountStatementItemsResponse(response);
    }
}
