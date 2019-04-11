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

    private static String toAuthKey(String username) {

        String padded = String.format("%010d", Long.parseLong(username));

        int[] digits = new int[10];
        digits[0] = Integer.parseInt(padded.substring(0, 1)) + 56;
        digits[1] = Integer.parseInt(padded.substring(1, 2)) + 57;
        digits[2] = Integer.parseInt(padded.substring(2, 3)) + 67;
        digits[3] = Integer.parseInt(padded.substring(3, 4)) + 51;
        digits[4] = Integer.parseInt(padded.substring(4, 5)) + 45;
        digits[5] = Integer.parseInt(padded.substring(5, 6)) + 45;
        digits[6] = Integer.parseInt(padded.substring(6, 7)) + 66;
        digits[7] = Integer.parseInt(padded.substring(7, 8)) + 80;
        digits[8] = Integer.parseInt(padded.substring(8, 9)) + 67;
        digits[9] = Integer.parseInt(padded.substring(9, 10)) + 69;

        StringBuilder retVal = new StringBuilder(2 * digits.length);
        for (int i = 0; i < digits.length; i++) {
            retVal.append(digits[i]);
        }

        return retVal.toString();
    }

    public AccountsResponse fetchAccounts(AccountsRequest request) {

        RequestBuilder builder = client.request(CaisseEpargneConstants.Url.WS_BAD);

        String response =
                builder.header(CaisseEpargneConstants.HeaderKey.SOAP_ACTION, request.action())
                        .body(this.write(request), MediaType.TEXT_XML)
                        .post(String.class);

        return read(response, AccountsResponse.class);
    }

    public TransactionsResponse fetchTransactions(TransactionsRequest request) {

        RequestBuilder builder = client.request(CaisseEpargneConstants.Url.WS_BAD);

        String response =
                builder.header(CaisseEpargneConstants.HeaderKey.SOAP_ACTION, request.action())
                        .body(this.write(request), MediaType.TEXT_XML)
                        .post(String.class);

        return read(response, TransactionsResponse.class);
    }

    public ClientInformationResponse getClientInformation(ClientInformationRequest request) {

        RequestBuilder builder = client.request(CaisseEpargneConstants.Url.WS_BAD);

        String response =
                builder.header(CaisseEpargneConstants.HeaderKey.SOAP_ACTION, request.action())
                        .body(this.write(request), MediaType.TEXT_XML)
                        .post(String.class);

        return this.read(response, ClientInformationResponse.class);
    }

    public DisconnectResponse disconnect(DisconnectRequest request) {

        RequestBuilder builder = client.request(CaisseEpargneConstants.Url.WS_BAD);

        String response =
                builder.header(CaisseEpargneConstants.HeaderKey.SOAP_ACTION, request.action())
                        .body(this.write(request), MediaType.TEXT_XML)
                        .post(String.class);

        return this.read(response, DisconnectResponse.class);
    }

    private <T> String write(T request) {

        String requestAsString = null;
        try {
            requestAsString =
                    mapper.writeValueAsString(request)
                            .replace(
                                    CaisseEpargneConstants.SoapXmlFragment.XMLNS_TO_REMOVE,
                                    CaisseEpargneConstants.SoapXmlFragment.XMLNS_TO_ADD);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }

        return CaisseEpargneConstants.SoapXmlFragment.PREFIX
                + requestAsString
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

    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        RequestBuilder builder = client.request(CaisseEpargneConstants.Url.WS_BAD);

        String response =
                builder.header(CaisseEpargneConstants.HeaderKey.SOAP_ACTION, request.action())
                        .header(
                                CaisseEpargneConstants.HeaderKey.X_AUTH_KEY,
                                toAuthKey(request.getUsername()))
                        .body(this.write(request), MediaType.TEXT_XML)
                        .post(String.class);

        return this.read(response, AuthenticationResponse.class);
    }
}
