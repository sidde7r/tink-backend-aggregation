package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

@Slf4j
public class NemIdTokenParser {

    private static final String STATUS_CODE_TAG = "sp:statuscode";
    private static final String STATUS_CODE_TAG_VALUE_ATTR = "value";
    private static final String STATUS_MESSAGE_TAG = "sp:statusmessage";
    private static final String SIGNATURE_PROPERTY_TAG = "ds:SignatureProperty";
    private static final String SIGNATURE_PROPERTY_NAME = "openoces:Name";
    private static final String REQUEST_ISSUER_TEXT = "RequestIssuer";
    private static final String SIGNATURE_PROPERTY_VALUE = "openoces:Value";

    NemIdTokenStatus extractNemIdTokenStatus(String nemIdTokenBase64) {
        String nemIdToken = decodeBase64(nemIdTokenBase64);
        Document rootElement = Jsoup.parse(nemIdToken);

        return NemIdTokenStatus.builder()
                .code(extractStatusCode(rootElement))
                .message(extractStatusMessage(rootElement))
                .requestIssuer(extractRequestIssuer(rootElement))
                .build();
    }

    private String extractStatusCode(Document rootElement) {
        Elements errorMessageTags = rootElement.getElementsByTag(STATUS_CODE_TAG);
        if (errorMessageTags.isEmpty()) {
            return "";
        }
        return errorMessageTags.get(0).attr(STATUS_CODE_TAG_VALUE_ATTR).trim();
    }

    private String extractStatusMessage(Document rootElement) {
        Elements errorMessageTags = rootElement.getElementsByTag(STATUS_MESSAGE_TAG);
        if (errorMessageTags.isEmpty()) {
            return "";
        }
        return errorMessageTags.get(0).ownText().trim();
    }

    private String extractRequestIssuer(Document rootElement) {
        Elements messageTags = rootElement.getElementsByTag(SIGNATURE_PROPERTY_TAG);
        return messageTags.stream()
                .filter(
                        tag ->
                                REQUEST_ISSUER_TEXT.equalsIgnoreCase(
                                        tag.getElementsByTag(SIGNATURE_PROPERTY_NAME).text()))
                .map(tag -> tag.getElementsByTag(SIGNATURE_PROPERTY_VALUE).text())
                .findFirst()
                .orElse("");
    }

    @SneakyThrows
    private String decodeBase64(String base64) {
        return new String(EncodingUtils.decodeBase64String(base64), StandardCharsets.UTF_8);
    }
}
