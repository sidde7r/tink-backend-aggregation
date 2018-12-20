package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk;

import com.google.common.base.Charsets;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
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
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.libraries.serialization.utils.SerializationUtils;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class BawagPskApiClient {

    private final TinkHttpClient client;
    private final SessionStorage storage;
    private final PersistentStorage persistentStorage;
    private final String baseUrl;
    private final String bankName;

    public BawagPskApiClient(
            final TinkHttpClient client,
            final SessionStorage storage,
            final PersistentStorage persistentStorage,
            final Provider provider) {
        this.client = client;
        this.storage = storage;
        this.persistentStorage = persistentStorage;

        final String[] providerStrings = provider.getPayload().split(",");
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
                String.format(
                        "https://%s%s", this.baseUrl, BawagPskConstants.URLS.SERVICE_ENDPOINT));
    }

    public String getBankName() {
        return this.bankName;
    }

    public LoginResponse login(final String requestString) throws HttpResponseException {
        final RequestBuilder requestBuilder = getRequest();

        final String responseString = requestBuilder.body(requestString).post(String.class);

        final Envelope envelope = BawagPskUtils.xmlToEntity(responseString, Envelope.class);

        final LoginResponse response = new LoginResponse(envelope);

        if (response.requestWasSuccessful()) {
            storage.put(
                    BawagPskConstants.Storage.SERVER_SESSION_ID.name(),
                    response.getServerSessionID());
            storage.put(BawagPskConstants.Storage.QID.name(), response.getQid());
            storage.put(
                    BawagPskConstants.Storage.PRODUCTS.name(),
                    response.getProducts()
                            .map(BawagPskUtils::entityToXml)
                            .orElseThrow(IllegalStateException::new));
            persistentStorage.put(
                    BawagPskConstants.Storage.PRODUCT_CODES.name(),
                    new JSONObject(response.getProductCodes()).toString());
        }
        return response;
    }

    public ServiceResponse checkIfSessionAlive() {
        // Using this message for the sole purpose of checking if session is alive
        final ServiceRequest request =
                new ServiceRequest(storage.get(BawagPskConstants.Storage.SERVER_SESSION_ID.name()));

        return new ServiceResponse(getRequest().post(Envelope.class, request.getXml()));
    }

    public void closeSession() {
        storage.clear();
    }

    public Optional<String> getFromSessionStorage(final String key) {
        return Optional.ofNullable(storage.get(key));
    }

    public LogoutResponse logout(final String requestString) {
        final Envelope response = getRequest().post(Envelope.class, requestString);
        storage.clear();
        return new LogoutResponse(response);
    }

    public GetAccountInformationListResponse getGetAccountInformationListResponse(
            final String requestString) {
        final String responseString = getRequest().body(requestString).post(String.class);

        final Envelope envelope = BawagPskUtils.xmlToEntity(responseString, Envelope.class);

        // TODO At this point, we could store the response so it can be reused by other fetchers
        // that are about to
        // execute after this one. However, this cannot (easily) be done until we have a storage
        // class for this purpose.

        return new GetAccountInformationListResponse(envelope);
    }

    private static String readFileContents(final String path) {
        try {
            return new String(FileUtils.readFileToByteArray(new File(path)), Charsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public GetAccountStatementItemsResponse getGetAccountStatementItemsResponse(
            final String requestString) {
        final String response = getRequest().body(requestString).post(String.class);

        final Envelope envelope = BawagPskUtils.xmlToEntity(response, Envelope.class);

        return new GetAccountStatementItemsResponse(envelope);
    }

    public Map<String, String> getProductCodes() {
        final String json = persistentStorage.get(BawagPskConstants.Storage.PRODUCT_CODES.name());
        return SerializationUtils.deserializeFromString(
                json, (new HashMap<String, String>()).getClass());
    }
}
