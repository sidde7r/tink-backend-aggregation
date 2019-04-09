package se.tink.backend.aggregation.agents.utils.authentication.encap;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.ActivationResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.SamlResponse;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.agents.utils.soap.SoapParser;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EncapUtils {
    private static final Pattern AUTHENTICATION_STATEMENT_PATTERN =
            Pattern.compile("<saml1:AuthenticationStatement(.+?)=\"");
    private static final String INVALID_USERNAME_OR_ACTIVATION_CODE = "251";
    private static final String SUCCEED = "0";

    static String buildApplicationHashAsB64String(String appId) {
        byte[] hash = Hash.sha256(appId);
        return EncodingUtils.encodeAsBase64String(hash);
    }

    static String buildRandom32BytesAsB64String() {
        byte[] randomBytes = RandomUtils.secureRandom(32);
        return EncodingUtils.encodeAsBase64String(randomBytes);
    }

    static void setMetaInformation(
            Map<String, String> queryPairs, Map<String, String> encapStorage) {
        queryPairs.put(
                "meta.applicationVersion",
                encapStorage.get(EncapConstants.Storage.APPLICATION_VERSION));
        queryPairs.put(
                "meta.encapAPIVersion", encapStorage.get(EncapConstants.Storage.ENCAP_API_VERSION));
    }

    static String getUrlEncodedQueryParams(Map<String, String> queryPairs) {
        Map<String, String> queryPairsWithUrlEncodedValues = Maps.newLinkedHashMap();
        queryPairs.forEach(
                (key, value) ->
                        queryPairsWithUrlEncodedValues.put(key, EncodingUtils.encodeUrl(value)));

        Joiner.MapJoiner joiner = Joiner.on("&").withKeyValueSeparator("=");
        return joiner.join(queryPairsWithUrlEncodedValues);
    }

    static Map<String, String> parseResponseQuery(String responseQuery) {
        Map<String, String> queryPairs = Maps.newHashMap();
        String[] pairs = responseQuery.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            queryPairs.put(
                    EncodingUtils.decodeUrl(pair.substring(0, idx)).toUpperCase(),
                    EncodingUtils.decodeUrl(pair.substring(idx + 1)));
        }

        return queryPairs;
    }

    static ActivationResponse parseActivationResponse(String response) {
        return parseResponseString(response, ActivationResponse.class);
    }

    static AuthenticationResponse parseAuthenticationResponse(String response) {
        return parseResponseString(response, AuthenticationResponse.class);
    }

    static SamlResponse parseSamlResponse(String response) {
        return parseResponseString(response, SamlResponse.class);
    }

    private static <T> T parseResponseString(String response, Class<T> responseType) {
        String prefixRemovedString = response.replaceFirst("^\\)]}'", "");

        return SerializationUtils.deserializeFromString(prefixRemovedString, responseType);
    }

    static String getSamlObjectFromResponseContent(String responseContentString) {
        Matcher matcher = AUTHENTICATION_STATEMENT_PATTERN.matcher(responseContentString);
        Preconditions.checkState(
                matcher.find(), "Could not parse samlObject from responseContent.");

        return matcher.group(1).trim();
    }

    static String getSamlObject(String responseString) {
        SamlResponse samlResponse = parseSamlResponse(responseString);
        return EncodingUtils.encodeAsBase64String(samlResponse.getResult().getResponseContent());
    }

    static String getActivationSessionId(String soapString) {
        Node node = SoapParser.getSoapBody(soapString);
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
            case SUCCEED:
                return element.getElementsByTagName("ns11:activationsession")
                        .item(0)
                        .getFirstChild()
                        .getTextContent();
            case INVALID_USERNAME_OR_ACTIVATION_CODE:
                return null;
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

    static List<String> getSecurityValuesList(String soapString) {
        List<String> valuesList = Lists.newArrayList();
        Node node = SoapParser.getSoapBody(soapString);
        Preconditions.checkState(
                node instanceof Element,
                "Could not parse security token and samUserId from server response.");

        Element element = (Element) node;
        Node securityTokenNode = element.getElementsByTagName("ns3:so").item(0);
        Node samUserIdNode = element.getElementsByTagName("ns3:samUserId").item(0);
        Preconditions.checkState(
                securityTokenNode != null, "Could not parse security token from server response.");
        Preconditions.checkState(
                samUserIdNode != null, "Could not parse samUserId from server response.");
        valuesList.add(securityTokenNode.getFirstChild().getTextContent());
        valuesList.add(samUserIdNode.getFirstChild().getTextContent());

        return valuesList;
    }

    static String getSecurityToken(String soapString) {
        Node node = SoapParser.getSoapBody(soapString);
        Preconditions.checkState(
                node instanceof Element, "Could not parse security token from server response.");

        Element element = (Element) node;
        Node securityTokenNode = element.getElementsByTagName("securityToken").item(0);
        Preconditions.checkState(
                securityTokenNode != null, "Could not parse security token from server response.");

        return securityTokenNode.getFirstChild().getTextContent();
    }
}
