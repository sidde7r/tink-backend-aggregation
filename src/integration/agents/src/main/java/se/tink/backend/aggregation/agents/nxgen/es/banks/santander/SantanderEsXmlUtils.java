package se.tink.backend.aggregation.agents.nxgen.es.banks.santander;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Preconditions;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import se.tink.backend.aggregation.agents.utils.soap.SoapParser;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SantanderEsXmlUtils {
    private static final XmlMapper MAPPER = new XmlMapper();

    public static <T> T deserializeFromSoapString(String data, String tagName, Class<T> cls) {
        Node node = getTagNodeFromSoapString(data, tagName);
        String serializedObject =
                SerializationUtils.deserializeFromString(
                        SerializationUtils.serializeToString(node), String.class);
        return parseXmlStringToJson(serializedObject, cls);
    }

    public static Node getTagNodeFromSoapString(String responseString, String tagName) {
        Node node = SoapParser.getSoapBody(responseString);
        Preconditions.checkState(
                node instanceof Element, "Could not parse SOAP body from server response.");

        Element element = (Element) node;
        return element.getElementsByTagName(tagName).item(0);
    }

    public static String parseJsonToXmlString(Object jsonObject) {
        try {
            JSONObject json = new JSONObject(SerializationUtils.serializeToString(jsonObject));
            return XML.toString(json);
        } catch (JSONException e) {
            throw new IllegalStateException("Could not parse JSON object to XML string.");
        }
    }

    public static <T> T parseXmlStringToJson(String xmlString, Class<T> returnType) {
        try {
            return MAPPER.readValue(xmlString, returnType);
        } catch (IOException e) {
            throw new IllegalStateException("Could not parse XML string into JSON object.");
        }
    }

    /**
     * It's necessary to serialize an XML node to a String and then deserialize it into a String if
     * we want to save it to the session storage. Otherwise we get a "String in a String" (i.e.
     * double quotes) when we fetch it from the session storage as a String.
     */
    public static String convertToString(Node node) {
        String serializedNode = SerializationUtils.serializeToString(node);

        return SerializationUtils.deserializeFromString(serializedNode, String.class);
    }
}
