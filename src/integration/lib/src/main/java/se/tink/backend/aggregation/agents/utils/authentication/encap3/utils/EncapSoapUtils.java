package se.tink.backend.aggregation.agents.utils.authentication.encap3.utils;

import com.google.common.base.Preconditions;
import java.util.Optional;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConfiguration;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConstants;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapStorage;
import se.tink.backend.aggregation.agents.utils.soap.SoapParser;

public class EncapSoapUtils {
    private final EncapConfiguration configuration;
    private final EncapStorage storage;

    public EncapSoapUtils(EncapConfiguration configuration, EncapStorage storage) {
        this.configuration = configuration;
        this.storage = storage;
    }

    // Encap does not provide any wsdl so that we can "easily" generate SOAP messages, which is why
    // they are static
    public String buildAuthSessionCreateRequest() {
        return "<soap:Envelope xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:n1=\"urn:sam.sec.fs.evry.com:ws:mobileauthentication:v3\" xmlns:n2=\"urn:sam.sec.fs.evry.com:domain:authentication:v2\" xmlns:n3=\"urn:mobile.security.fs.edb.com:domain:mobileapplication:v1\" xmlns:n4=\"http://edb.com/ws/WSCommon_v21\">"
                + "<soap:Header>"
                + "<n4:AutHeader>"
                + "<n4:SourceApplication>SAMOBILE</n4:SourceApplication>"
                + "<n4:DestinationApplication>SECPSAM</n4:DestinationApplication>"
                + "<n4:Function>SECSMobileAuthenticationService_V3_0</n4:Function>"
                + "<n4:Version>1.0.0</n4:Version>"
                + "<n4:ClientContext>"
                + String.format(
                        "<n4:userid>%s</n4:userid>",
                        StringEscapeUtils.escapeXml10(storage.getUsername()))
                + String.format(
                        "<n4:credentials>%s</n4:credentials>",
                        StringEscapeUtils.escapeXml10(configuration.getCredentialsAppNameForEdb()))
                + "<n4:channel>MOB</n4:channel>"
                + String.format(
                        "<n4:orgid>%s</n4:orgid>",
                        StringEscapeUtils.escapeXml10(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<n4:orgunit>%s</n4:orgunit>",
                        StringEscapeUtils.escapeXml10(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<n4:customerid>%s</n4:customerid>",
                        StringEscapeUtils.escapeXml10(storage.getUsername()))
                + "<n4:locale>en_SE</n4:locale>"
                + "<n4:ip>127.0.0.1</n4:ip>"
                + "</n4:ClientContext>"
                + "</n4:AutHeader>"
                + "</soap:Header>"
                + "<soap:Body>"
                + "<n1:mobileAuthSessionCreateRequest>"
                + String.format(
                        "<n2:userName>%s</n2:userName>",
                        StringEscapeUtils.escapeXml10(storage.getUsername()))
                + String.format(
                        "<n3:applicationName>%s</n3:applicationName>",
                        StringEscapeUtils.escapeXml10(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<n1:hardwareId>%s</n1:hardwareId>",
                        StringEscapeUtils.escapeXml10(storage.getHardwareId()))
                + "</n1:mobileAuthSessionCreateRequest>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }

    // Encap does not provide any wsdl so that we can "easily" generate SOAP messages, which is why
    // they are static
    public String buildActivationSessionUpdateRequest(String activationCode) {
        return "<soap:Envelope xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:n1=\"urn:sam.sec.fs.evry.com:ws:mobileactivation:v2\" xmlns:n2=\"urn:sam.sec.fs.evry.com:domain:activation:v1\" xmlns:n3=\"http://edb.com/ws/WSCommon_v21\">"
                + "<soap:Header>"
                + "<n3:AutHeader>"
                + "<n3:SourceApplication>SAMOBILE</n3:SourceApplication>"
                + "<n3:DestinationApplication>SECPSAM</n3:DestinationApplication>"
                + "<n3:Function>SECSMobileActivationService_V2_0</n3:Function>"
                + "<n3:Version>1.0.0</n3:Version>"
                + "<n3:ClientContext>"
                + String.format(
                        "<n3:userid>%s</n3:userid>",
                        StringEscapeUtils.escapeXml10(storage.getUsername()))
                + String.format(
                        "<n3:credentials>%s</n3:credentials>",
                        StringEscapeUtils.escapeXml10(configuration.getCredentialsAppNameForEdb()))
                + "<n3:channel>MOB</n3:channel>"
                + String.format(
                        "<n3:orgid>%s</n3:orgid>",
                        StringEscapeUtils.escapeXml10(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<n3:orgunit>%s</n3:orgunit>",
                        StringEscapeUtils.escapeXml10(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<n3:customerid>%s</n3:customerid>",
                        StringEscapeUtils.escapeXml10(storage.getUsername()))
                + "<n3:locale>en_SE</n3:locale>"
                + "<n3:ip>127.0.0.1</n3:ip>"
                + "</n3:ClientContext>"
                + "</n3:AutHeader>"
                + "</soap:Header>"
                + "<soap:Body>"
                + "<n1:mobileActivationSessionUpdateRequest>"
                + String.format(
                        "<n2:userName>%s</n2:userName>",
                        StringEscapeUtils.escapeXml10(storage.getUsername()))
                + String.format(
                        "<n2:applicationName>%s</n2:applicationName>",
                        StringEscapeUtils.escapeXml10(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<n1:activationCode>%s</n1:activationCode>",
                        StringEscapeUtils.escapeXml10(activationCode))
                + String.format(
                        "<n1:hardwareId>%s</n1:hardwareId>",
                        StringEscapeUtils.escapeXml10(storage.getHardwareId()))
                + "</n1:mobileActivationSessionUpdateRequest>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }

    // Encap does not provide any wsdl so that we can "easily" generate SOAP messages, which is why
    // they are static
    public String buildActivationCreateRequest(
            String username, String activationSessionId, String samlObjectB64) {
        return "<soap:Envelope xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:n1=\"urn:sam.sec.fs.evry.com:ws:mobileactivation:v2\" xmlns:n2=\"urn:sam.sec.fs.evry.com:domain:activation:v1\" xmlns:n3=\"urn:sam.sec.fs.evry.com:domain:authentication:v2\" xmlns:n4=\"http://edb.com/ws/WSCommon_v21\">"
                + "<soap:Header>"
                + "<n4:AutHeader>"
                + "<n4:SourceApplication>SAMOBILE</n4:SourceApplication>"
                + "<n4:DestinationApplication>SECPSAM</n4:DestinationApplication>"
                + "<n4:Function>SECSMobileActivationService_V2_0</n4:Function>"
                + "<n4:Version>1.0.0</n4:Version>"
                + "<n4:ClientContext>"
                + String.format(
                        "<n4:userid>%s</n4:userid>", StringEscapeUtils.escapeXml10(username))
                + String.format(
                        "<n4:credentials>%s</n4:credentials>",
                        StringEscapeUtils.escapeXml10(configuration.getCredentialsAppNameForEdb()))
                + "<n4:channel>MOB</n4:channel>"
                + String.format(
                        "<n4:orgid>%s</n4:orgid>",
                        StringEscapeUtils.escapeXml10(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<n4:orgunit>%s</n4:orgunit>",
                        StringEscapeUtils.escapeXml10(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<n4:customerid>%s</n4:customerid>",
                        StringEscapeUtils.escapeXml10(username))
                + "<n4:locale>en_SE</n4:locale>"
                + "<n4:ip>127.0.0.1</n4:ip>"
                + "</n4:ClientContext>"
                + "</n4:AutHeader>"
                + "</soap:Header>"
                + "<soap:Body>"
                + "<n1:mobileActivationCreateRequest>"
                + String.format(
                        "<n2:activationSessionId>%s</n2:activationSessionId>",
                        StringEscapeUtils.escapeXml10(activationSessionId))
                + String.format(
                        "<n1:samlObject>%s</n1:samlObject>",
                        StringEscapeUtils.escapeXml10(samlObjectB64))
                + "<n3:tokenType>SO</n3:tokenType>"
                + "</n1:mobileActivationCreateRequest>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }

    // Encap does not provide any wsdl so that we can "easily" generate SOAP messages, which is why
    // they are static
    public String buildAuthSessionReadRequest(String username, String samlObjectB64) {
        return "<soap:Envelope xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:n1=\"urn:sam.sec.fs.evry.com:ws:mobileauthentication:v3\" xmlns:n2=\"urn:sam.sec.fs.evry.com:domain:authentication:v2\" xmlns:n3=\"urn:mobile.security.fs.edb.com:domain:mobileapplication:v1\" xmlns:n4=\"http://edb.com/ws/WSCommon_v21\">"
                + "<soap:Header>"
                + "<n4:AutHeader>"
                + "<n4:SourceApplication>SAMOBILE</n4:SourceApplication>"
                + "<n4:DestinationApplication>SECPSAM</n4:DestinationApplication>"
                + "<n4:Function>SECSMobileAuthenticationService_V3_0</n4:Function>"
                + "<n4:Version>1.0.0</n4:Version>"
                + "<n4:ClientContext>"
                + String.format(
                        "<n4:userid>%s</n4:userid>", StringEscapeUtils.escapeXml10(username))
                + String.format(
                        "<n4:credentials>%s</n4:credentials>",
                        StringEscapeUtils.escapeXml10(configuration.getCredentialsAppNameForEdb()))
                + "<n4:channel>MOB</n4:channel>"
                + String.format(
                        "<n4:orgid>%s</n4:orgid>",
                        StringEscapeUtils.escapeXml10(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<n4:orgunit>%s</n4:orgunit>",
                        StringEscapeUtils.escapeXml10(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<n4:customerid>%s</n4:customerid>",
                        StringEscapeUtils.escapeXml10(username))
                + "<n4:locale>en_SE</n4:locale>"
                + "<n4:ip>127.0.0.1</n4:ip>"
                + "</n4:ClientContext>"
                + "</n4:AutHeader>"
                + "</soap:Header>"
                + "<soap:Body>"
                + "<n1:mobileAuthSessionReadRequest>"
                + String.format(
                        "<n2:userName>%s</n2:userName>", StringEscapeUtils.escapeXml10(username))
                + String.format(
                        "<n3:applicationName>%s</n3:applicationName>",
                        StringEscapeUtils.escapeXml10(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<n1:hardwareId>%s</n1:hardwareId>",
                        StringEscapeUtils.escapeXml10(storage.getHardwareId()))
                + String.format(
                        "<n1:samlObject>%s</n1:samlObject>",
                        StringEscapeUtils.escapeXml10(samlObjectB64))
                + "<n2:tokenType>SO</n2:tokenType>"
                + "</n1:mobileAuthSessionReadRequest>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }

    public static Optional<String> getActivationSessionId(String soapResponse) {
        Node node = SoapParser.getSoapBody(soapResponse);
        Preconditions.checkState(
                node instanceof Element,
                "Could not parse activationSessionId from server response.");

        Element element = (Element) node;
        String errorCode =
                element.getElementsByTagName("ErrorCode").item(0).getFirstChild().getTextContent();

        switch (errorCode) {
            case EncapConstants.Soap.EC_SUCCESS:
                return Optional.ofNullable(
                        element.getElementsByTagName("ns2:activationSessionId")
                                .item(0)
                                .getFirstChild()
                                .getTextContent());
            case EncapConstants.Soap.EC_INVALID_USERNAME_OR_ACTIVATION_CODE:
                return Optional.empty();
            default:
                String errorMessage =
                        element.getElementsByTagName("Message")
                                .item(0)
                                .getFirstChild()
                                .getTextContent();

                throw new IllegalStateException(
                        String.format(
                                "Unexpected error during activation: (%s) %s ",
                                errorCode, errorMessage));
        }
    }

    public static Optional<String> getSecurityToken(String soapResponse) {
        Node node = SoapParser.getSoapBody(soapResponse);
        Preconditions.checkState(
                node instanceof Element, "Could not parse security token from server response.");

        Element element = (Element) node;
        Node securityTokenNode = element.getElementsByTagNameNS("*", "so").item(0);

        Preconditions.checkNotNull(
                securityTokenNode, "Could not parse security token from server response.");

        return Optional.ofNullable(securityTokenNode.getFirstChild().getTextContent());
    }

    public static Optional<String> getSamUserId(String soapResponse) {
        Node node = SoapParser.getSoapBody(soapResponse);
        Preconditions.checkState(
                node instanceof Element, "Could not parse samUserId from server response.");

        Element element = (Element) node;
        Node samUserIdNode = element.getElementsByTagNameNS("*", "samUserId").item(0);

        Preconditions.checkNotNull(
                samUserIdNode, "Could not parse samUserId from server response.");

        return Optional.ofNullable(samUserIdNode.getFirstChild().getTextContent());
    }
}
