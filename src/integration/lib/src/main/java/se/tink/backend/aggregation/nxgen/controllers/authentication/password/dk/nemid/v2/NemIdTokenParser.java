package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

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

    public NemIdTokenStatus extractNemIdTokenStatus(String nemIdTokenBase64) {
        String nemIdToken = decodeBase64(nemIdTokenBase64);
        Document rootElement = Jsoup.parse(nemIdToken);

        return NemIdTokenStatus.builder()
                .code(extractStatusCode(rootElement))
                .message(extractStatusMessage(rootElement))
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

    @SneakyThrows
    private String decodeBase64(String base64) {
        return new String(EncodingUtils.decodeBase64String(base64), StandardCharsets.UTF_8);
    }
}
