package se.tink.backend.aggregation.agents.utils.authentication.encap2.utils;

import com.google.common.base.Preconditions;
import java.util.Optional;
import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.EncapConfiguration;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.EncapConstants;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.EncapStorage;
import se.tink.backend.aggregation.agents.utils.soap.SoapParser;

public class EncapSoapUtils {
    private final EncapConfiguration configuration;
    private final EncapStorage storage;

    public EncapSoapUtils(EncapConfiguration configuration, EncapStorage storage) {
        this.configuration = configuration;
        this.storage = storage;
    }

    public String buildAuthenticationSessionCreateV1Body(String username) {
        String encryptedEdbCredentials =
                EncapCryptoUtils.computeEncryptedEdbCredentials(
                        configuration.getCredentialsBankCodeForEdb(),
                        storage.getHardwareId(),
                        configuration.getClientPrivateKeyString(),
                        configuration.getRsaPubKeyString());

        return "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SECSMobileAuthenticationSessionCreate_V1_0Service=\"http://mobileactivation.security.edb.com\" xmlns:edb=\"http://edb.com/ws/WSCommon_v21\" xsl:version=\"1.0\">"
                + "<soap:Header>"
                + "<edb:AutHeader>"
                + "<edb:SourceApplication>SAMOBILE</edb:SourceApplication>"
                + "<edb:DestinationApplication>SA</edb:DestinationApplication>"
                + "<edb:Function>SECSMobileAuthenticationSessionCreate_V1_0Service</edb:Function>"
                + "<edb:Version>1.0.0</edb:Version>"
                + "<edb:ClientContext>"
                + String.format(
                        "<edb:userid>%s</edb:userid>", StringEscapeUtils.escapeXml(username))
                + String.format(
                        "<edb:credentials>%s;%s</edb:credentials>",
                        StringEscapeUtils.escapeXml(configuration.getSaIdentifier()),
                        StringEscapeUtils.escapeXml(encryptedEdbCredentials))
                + "<edb:channel>MOB</edb:channel>"
                + String.format(
                        "<edb:orgid>%s</edb:orgid>",
                        StringEscapeUtils.escapeXml(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<edb:orgunit>%s</edb:orgunit>",
                        StringEscapeUtils.escapeXml(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<edb:customerid>%s</edb:customerid>",
                        StringEscapeUtils.escapeXml(username))
                + "<edb:locale>en_SE</edb:locale>"
                + "<edb:ip>192.168.0.1</edb:ip>"
                + "</edb:ClientContext>"
                + "</edb:AutHeader>"
                + "</soap:Header>"
                + "<soap:Body>"
                + "<SECSMobileAuthenticationSessionCreate_V1_0Service:SECSMobileAuthenticationSessionCreate_V1_0InputArgs>"
                + "<SECSMobileAuthenticationSessionCreate_V1_0Service:MobileAuthenticationSessionCreateRequest>"
                + String.format(
                        "<SECSMobileAuthenticationSessionCreate_V1_0Service:username>%s</SECSMobileAuthenticationSessionCreate_V1_0Service:username>",
                        StringEscapeUtils.escapeXml(username))
                + String.format(
                        "<SECSMobileAuthenticationSessionCreate_V1_0Service:application>%s</SECSMobileAuthenticationSessionCreate_V1_0Service:application>",
                        StringEscapeUtils.escapeXml(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<SECSMobileAuthenticationSessionCreate_V1_0Service:hardwareid>%s</SECSMobileAuthenticationSessionCreate_V1_0Service:hardwareid>",
                        StringEscapeUtils.escapeXml(storage.getHardwareId()))
                + "</SECSMobileAuthenticationSessionCreate_V1_0Service:MobileAuthenticationSessionCreateRequest>"
                + "</SECSMobileAuthenticationSessionCreate_V1_0Service:SECSMobileAuthenticationSessionCreate_V1_0InputArgs>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }

    // Encap does not provide any wsdl so that we can "easily" generate SOAP messages, which is why
    // they are static
    public String buildActivationSessionUpdateV1Body(String username, String activationCode) {
        String encryptedEdbCredentials =
                EncapCryptoUtils.computeEncryptedEdbCredentials(
                        configuration.getCredentialsBankCodeForEdb(),
                        storage.getHardwareId(),
                        configuration.getClientPrivateKeyString(),
                        configuration.getRsaPubKeyString());

        return "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SECSMobileActivationSessionUpdate_V1_0Service=\"http://mobileactivation.security.edb.com\" xmlns:edb=\"http://edb.com/ws/WSCommon_v21\" xsl:version=\"1.0\">"
                + "<soap:Header>"
                + "<edb:AutHeader>"
                + "<edb:SourceApplication>SAMOBILE</edb:SourceApplication>"
                + "<edb:DestinationApplication>SA</edb:DestinationApplication>"
                + "<edb:Function>SECSMobileActivationSessionUpdate_V1_0Service</edb:Function>"
                + "<edb:Version>1.0.0</edb:Version>"
                + "<edb:ClientContext>"
                + String.format(
                        "<edb:userid>%s</edb:userid>", StringEscapeUtils.escapeXml(username))
                + String.format(
                        "<edb:credentials>%s;%s</edb:credentials>",
                        StringEscapeUtils.escapeXml(configuration.getSaIdentifier()),
                        StringEscapeUtils.escapeXml(encryptedEdbCredentials))
                + "<edb:channel>MOB</edb:channel>"
                + String.format(
                        "<edb:orgid>%s</edb:orgid>",
                        StringEscapeUtils.escapeXml(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<edb:orgunit>%s</edb:orgunit>",
                        StringEscapeUtils.escapeXml(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<edb:customerid>%s</edb:customerid>",
                        StringEscapeUtils.escapeXml(username))
                + "<edb:locale>en_SE</edb:locale>"
                + "<edb:ip>192.168.0.1</edb:ip>"
                + "</edb:ClientContext>"
                + "</edb:AutHeader>"
                + "</soap:Header>"
                + "<soap:Body>"
                + "<SECSMobileActivationSessionUpdate_V1_0Service:SECSMobileActivationSessionUpdate_V1_0InputArgs>"
                + "<SECSMobileActivationSessionUpdate_V1_0Service:MobileActivationSessionUpdateRequest>"
                + String.format(
                        "<SECSMobileActivationSessionUpdate_V1_0Service:username>%s</SECSMobileActivationSessionUpdate_V1_0Service:username>",
                        StringEscapeUtils.escapeXml(username))
                + String.format(
                        "<SECSMobileActivationSessionUpdate_V1_0Service:application>%s</SECSMobileActivationSessionUpdate_V1_0Service:application>",
                        StringEscapeUtils.escapeXml(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<SECSMobileActivationSessionUpdate_V1_0Service:activationcode>%s</SECSMobileActivationSessionUpdate_V1_0Service:activationcode>",
                        StringEscapeUtils.escapeXml(activationCode))
                + String.format(
                        "<SECSMobileActivationSessionUpdate_V1_0Service:hardwareid>%s</SECSMobileActivationSessionUpdate_V1_0Service:hardwareid>",
                        StringEscapeUtils.escapeXml(storage.getHardwareId()))
                + "</SECSMobileActivationSessionUpdate_V1_0Service:MobileActivationSessionUpdateRequest>"
                + "</SECSMobileActivationSessionUpdate_V1_0Service:SECSMobileActivationSessionUpdate_V1_0InputArgs>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }

    // Encap does not provide any wsdl so that we can "easily" generate SOAP messages, which is why
    // they are static
    public String buildActivationCreateV1Body(
            String username, String activationSessionId, String samlObjectB64) {
        String encryptedEdbCredentials =
                EncapCryptoUtils.computeEncryptedEdbCredentials(
                        configuration.getCredentialsBankCodeForEdb(),
                        storage.getHardwareId(),
                        configuration.getClientPrivateKeyString(),
                        configuration.getRsaPubKeyString());

        return "<soap:Envelope xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:n1=\"urn:sam.sec.fs.evry.com:ws:mobileactivation:v1\" xmlns:n2=\"urn:sam.sec.fs.evry.com:domain:activation:v1\" xmlns:n3=\"http://edb.com/ws/WSCommon_v21\">"
                + "<soap:Header>"
                + "<n3:AutHeader>"
                + "<n3:SourceApplication>SAMOBILE</n3:SourceApplication>"
                + "<n3:DestinationApplication>SECPSAM</n3:DestinationApplication>"
                + "<n3:Function>SECSMobileActivationService_V1_0</n3:Function>"
                + "<n3:Version>1.0.0</n3:Version>"
                + "<n3:ClientContext>"
                + String.format("<n3:userid>%s</n3:userid>", StringEscapeUtils.escapeXml(username))
                + String.format(
                        "<n3:credentials>%s;%s</n3:credentials>",
                        StringEscapeUtils.escapeXml(configuration.getSaIdentifier()),
                        StringEscapeUtils.escapeXml(encryptedEdbCredentials))
                + "<n3:channel>MOB</n3:channel>"
                + String.format(
                        "<n3:orgid>%s</n3:orgid>",
                        StringEscapeUtils.escapeXml(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<n3:orgunit>%s</n3:orgunit>",
                        StringEscapeUtils.escapeXml(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<n3:customerid>%s</n3:customerid>", StringEscapeUtils.escapeXml(username))
                + "<n3:locale>en_SE</n3:locale>"
                + "<n3:ip>192.168.0.1</n3:ip>"
                + "</n3:ClientContext>"
                + "</n3:AutHeader>"
                + "</soap:Header>"
                + "<soap:Body>"
                + "<n1:mobileActivationCreateRequest>"
                + String.format(
                        "<n2:activationSessionId>%s</n2:activationSessionId>",
                        StringEscapeUtils.escapeXml(activationSessionId))
                + String.format(
                        "<n1:samlObject>%s</n1:samlObject>",
                        StringEscapeUtils.escapeXml(samlObjectB64))
                + "</n1:mobileActivationCreateRequest>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }

    // Encap does not provide any wsdl so that we can "easily" generate SOAP messages, which is why
    // they are static
    public String buildAuthenticationV2Body(String username, String samlObjectB64) {
        String encryptedEdbCredentials =
                EncapCryptoUtils.computeEncryptedEdbCredentials(
                        configuration.getCredentialsBankCodeForEdb(),
                        storage.getHardwareId(),
                        configuration.getClientPrivateKeyString(),
                        configuration.getRsaPubKeyString());

        return "<soap:Envelope xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:n1=\"urn:sam.sec.fs.evry.com:ws:mobileauthentication:v2\" xmlns:n2=\"urn:sam.sec.fs.evry.com:domain:authentication:v1\" xmlns:n3=\"urn:mobile.security.fs.edb.com:domain:mobileapplication:v1\" xmlns:n4=\"http://edb.com/ws/WSCommon_v21\">"
                + "<soap:Header>"
                + "<n4:AutHeader>"
                + "<n4:SourceApplication>SAMOBILE</n4:SourceApplication>"
                + "<n4:DestinationApplication>SECPSAM</n4:DestinationApplication>"
                + "<n4:Function>SECSMobileAuthenticationService_V2_0</n4:Function>"
                + "<n4:Version>1.0.0</n4:Version>"
                + "<n4:ClientContext>"
                + String.format("<n4:userid>%s</n4:userid>", StringEscapeUtils.escapeXml(username))
                + String.format(
                        "<n4:credentials>%s;%s</n4:credentials>",
                        StringEscapeUtils.escapeXml(configuration.getSaIdentifier()),
                        StringEscapeUtils.escapeXml(encryptedEdbCredentials))
                + "<n4:channel>MOB</n4:channel>"
                + String.format(
                        "<n4:orgid>%s</n4:orgid>",
                        StringEscapeUtils.escapeXml(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<n4:orgunit>%s</n4:orgunit>",
                        StringEscapeUtils.escapeXml(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<n4:customerid>%s</n4:customerid>", StringEscapeUtils.escapeXml(username))
                + "<n4:locale>en_SE</n4:locale>"
                + "<n4:ip>127.0.0.1</n4:ip>"
                + "</n4:ClientContext>"
                + "</n4:AutHeader>"
                + "</soap:Header>"
                + "<soap:Body>"
                + "<n1:mobileAuthSessionReadRequest>"
                + String.format(
                        "<n2:userName>%s</n2:userName>", StringEscapeUtils.escapeXml(username))
                + String.format(
                        "<n3:applicationName>%s</n3:applicationName>",
                        StringEscapeUtils.escapeXml(configuration.getCredentialsAppNameForEdb()))
                + String.format(
                        "<n1:hardwareId>%s</n1:hardwareId>",
                        StringEscapeUtils.escapeXml(storage.getHardwareId()))
                + String.format(
                        "<n1:samlObject>%s</n1:samlObject>",
                        StringEscapeUtils.escapeXml(samlObjectB64))
                + "</n1:mobileAuthSessionReadRequest>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }

    public Optional<String> getActivationSessionId(String soapResponse) {
        Node node = SoapParser.getSoapBody(soapResponse);
        Preconditions.checkState(
                node instanceof Element,
                "Could not parse activationSessionId from server response.");

        Element element = (Element) node;
        String errorCode =
                element.getElementsByTagName("ns2:ErrorCode")
                        .item(0)
                        .getFirstChild()
                        .getTextContent();

        switch (errorCode) {
            case EncapConstants.Soap.EC_SUCCESS:
                return Optional.ofNullable(
                        element.getElementsByTagName("ns11:activationsession")
                                .item(0)
                                .getFirstChild()
                                .getTextContent());
            case EncapConstants.Soap.EC_INVALID_USERNAME_OR_ACTIVATION_CODE:
                return Optional.empty();
            default:
                String errorMessage =
                        element.getElementsByTagName("ns2:Message")
                                .item(0)
                                .getFirstChild()
                                .getTextContent();

                throw new IllegalStateException(
                        String.format(
                                "Unexpected error during activation: (%s) %s ",
                                errorCode, errorMessage));
        }
    }

    public Optional<String> getSecurityToken(String soapResponse) {
        Node node = SoapParser.getSoapBody(soapResponse);
        Preconditions.checkState(
                node instanceof Element, "Could not parse security token from server response.");

        Element element = (Element) node;
        Node securityTokenNode = element.getElementsByTagNameNS("*", "so").item(0);

        Preconditions.checkNotNull(
                securityTokenNode, "Could not parse security token from server response.");

        return Optional.ofNullable(securityTokenNode.getFirstChild().getTextContent());
    }

    public Optional<String> getSamUserId(String soapResponse) {
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
