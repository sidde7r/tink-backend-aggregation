package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
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
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.serialization.TypeReferences;

public class CaisseEpargneApiClient {

    private static final String PREFIX =
            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><soap:Body>";
    private static final String POSTFIX = "</soap:Body></soap:Envelope>";

    private final XmlMapper mapper = new XmlMapper();
    private final TinkHttpClient client;
    private final XMLInputFactory inputFactory = XMLInputFactory.newFactory();

    public CaisseEpargneApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public AuthenticationResponse postAuthentication(AuthenticationRequest request) {

        RequestBuilder builder = client.request(CaisseEpargneConstants.Url.WS_BAD);

        String response = builder
                .header(CaisseEpargneConstants.Header.SOAP_ACTION, request.action())
                .header(CaisseEpargneConstants.Header.X_AUTH_KEY,
                        CaisseEpargneConstants.Default.X_AUTH_KEY)
                .body(this.write(request), MediaType.TEXT_XML)
                .post(String.class);

        Map<String, Object> result = this.read(response);
        return new AuthenticationResponse(result);
    }

    public ClientInformationResponse postClientInformation(ClientInformationRequest request) {

        RequestBuilder builder = client.request(CaisseEpargneConstants.Url.WS_BAD);

        String response = builder
                .header(CaisseEpargneConstants.Header.SOAP_ACTION, request.action())
                .body(this.write(request), MediaType.TEXT_XML)
                .post(String.class);

        Map<String, Object> result = this.read(response);
        return new ClientInformationResponse(result);
    }

    public DisconnectResponse postDisconnect(DisconnectRequest request) {

        RequestBuilder builder = client.request(CaisseEpargneConstants.Url.WS_BAD);

        String response = builder
                .header(CaisseEpargneConstants.Header.SOAP_ACTION, request.action())
                .body(this.write(request), MediaType.TEXT_XML)
                .post(String.class);

        Map<String, Object> result = this.read(response);
        return new DisconnectResponse(result);
    }

    private <T> String write(T request) {

        String requestAsString = null;
        try {
            requestAsString = mapper.writeValueAsString(request)
                    .replace("xmlns=\"\"", "xmlns=\"http://caisse-epargne.fr/webservices/\"");
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }

        return PREFIX + requestAsString + POSTFIX;
    }

    private Map<String, Object> read(String response) {

        Map<String, Object> retVal = null;

        try (StringReader stringReader = new StringReader(response)) {
            XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(stringReader);
            xmlReader.next();
            xmlReader.next();
            xmlReader.next();
            xmlReader.next();
            retVal = mapper.readValue(xmlReader, TypeReferences.MAP_OF_STRING_OBJECT);
            xmlReader.close();
        } catch (XMLStreamException | IOException e) {
            throw new IllegalStateException(e);
        }

        return retVal;
    }
}
