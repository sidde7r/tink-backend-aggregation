package se.tink.backend.aggregation.agents.creditcards.supremecard.v2;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class SupremeCardParsingUtils {
    static Optional<String> parseBankIdUrl(String htmlResponse) {
        Document document = Jsoup.parse(htmlResponse);

        return document != null
                ? Optional.of(
                        document.select(
                                        "[href*="
                                                + SupremeCardApiConstants.BANKID_QUERY_PARAMETER
                                                + "]")
                                .get(0)
                                .attr("href"))
                : Optional.empty();
    }

    static Optional<Map<String, String>> parseSignicatFields(String htmlResponse) {
        Document document = Jsoup.parse(htmlResponse);

        if (document == null) {
            return Optional.empty();
        }

        Map<String, String> signicatMap = Maps.newHashMap();
        for (String signicatValue :
                document.getElementsByTag("script")
                        .get(4)
                        .dataNodes()
                        .get(0)
                        .getWholeData()
                        .replaceAll("\n", "")
                        .replaceAll(" ", "")
                        .split(";")) {
            String[] signicatArray = signicatValue.split("=");
            signicatMap.put(signicatArray[0], signicatArray[1].replaceAll("'", ""));
        }

        return signicatMap.isEmpty() ? Optional.empty() : Optional.of(signicatMap);
    }

    static Optional<Map<String, String>> parseSAMLResponseAndTargetURL(String htmlResponse) {
        Map<String, String> queryParameters = Maps.newHashMap();

        Document document = Jsoup.parse(htmlResponse);

        if (document == null) {
            return Optional.empty();
        }

        String samlResponse =
                document.getElementsByTag("form")
                        .get(0)
                        .getElementsByTag("input")
                        .select(
                                "[name*="
                                        + SupremeCardApiConstants.SAML_RESPONSE_PARAMETER_KEY
                                        + "]")
                        .get(0)
                        .attr("value");

        String target =
                document.getElementsByTag("form")
                        .get(0)
                        .getElementsByTag("input")
                        .select("[name*=" + SupremeCardApiConstants.TARGET_PARAMETER_KEY + "]")
                        .get(0)
                        .attr("value");

        queryParameters.put(SupremeCardApiConstants.SAML_RESPONSE_PARAMETER_KEY, samlResponse);
        queryParameters.put(SupremeCardApiConstants.TARGET_PARAMETER_KEY, target);

        return queryParameters.isEmpty() ? Optional.empty() : Optional.of(queryParameters);
    }
}
