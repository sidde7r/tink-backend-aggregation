package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.IOException;
import java.io.StringReader;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.AuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.ClientInformationRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.ClientInformationResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.DisconnectRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.DisconnectResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc.AccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc.TransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class CaisseEpargneApiClient {

    private final XmlMapper mapper = new XmlMapper();
    private final TinkHttpClient client;
    private final XMLInputFactory inputFactory = XMLInputFactory.newFactory();

    public CaisseEpargneApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        RequestBuilder builder = client.request(CaisseEpargneConstants.Url.WS_BAD);

        String response = builder
                .header(CaisseEpargneConstants.HeaderKey.SOAP_ACTION, request.action())
                .header(CaisseEpargneConstants.HeaderKey.X_AUTH_KEY,
                        CaisseEpargneConstants.HeaderValue.X_AUTH_KEY)
                .body(this.write(request), MediaType.TEXT_XML)
                .post(String.class);

        return this.read(response, AuthenticationResponse.class);
    }

    public AccountsResponse fetchAccounts(AccountsRequest request) {

        RequestBuilder builder = client.request(CaisseEpargneConstants.Url.WS_BAD);

        String response = builder
                .header(CaisseEpargneConstants.HeaderKey.SOAP_ACTION, request.action())
                .body(this.write(request), MediaType.TEXT_XML)
                .post(String.class);

        return read(response, AccountsResponse.class);
    }

    public TransactionsResponse fetchTransactions(TransactionsRequest request) {

        RequestBuilder builder = client.request(CaisseEpargneConstants.Url.WS_BAD);

        String response = builder
                .header(CaisseEpargneConstants.HeaderKey.SOAP_ACTION, request.action())
                .body(this.write(request), MediaType.TEXT_XML)
                .post(String.class);

        return read(response, TransactionsResponse.class);
    }

    public ClientInformationResponse getClientInformation(ClientInformationRequest request) {

        RequestBuilder builder = client.request(CaisseEpargneConstants.Url.WS_BAD);

        String response = builder
                .header(CaisseEpargneConstants.HeaderKey.SOAP_ACTION, request.action())
                .body(this.write(request), MediaType.TEXT_XML)
                .post(String.class);

        return this.read(response, ClientInformationResponse.class);
    }

    public DisconnectResponse disconnect(DisconnectRequest request) {

        RequestBuilder builder = client.request(CaisseEpargneConstants.Url.WS_BAD);

        String response = builder
                .header(CaisseEpargneConstants.HeaderKey.SOAP_ACTION, request.action())
                .body(this.write(request), MediaType.TEXT_XML)
                .post(String.class);

        return this.read(response, DisconnectResponse.class);
    }

    private <T> String write(T request) {

        String requestAsString = null;
        try {
            requestAsString = mapper.writeValueAsString(request)
                    .replace(CaisseEpargneConstants.SoapXmlFragment.XMLNS_TO_REMOVE,
                            CaisseEpargneConstants.SoapXmlFragment.XMLNS_TO_ADD);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }

        return CaisseEpargneConstants.SoapXmlFragment.PREFIX + requestAsString
                + CaisseEpargneConstants.SoapXmlFragment.POSTFIX;
    }

    private <T> T read(String response, Class<T> type) {

        T retVal = null;

        try (StringReader stringReader = new StringReader(response)) {
            XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(stringReader);
            xmlReader.next();
            xmlReader.next();
            xmlReader.next();
            xmlReader.next();
            retVal = mapper.readValue(xmlReader, type);
            xmlReader.close();
        } catch (XMLStreamException | IOException e) {
            throw new IllegalStateException(e);
        }

        return retVal;
    }
}
