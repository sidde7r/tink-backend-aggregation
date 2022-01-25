package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.mainview;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class FormElementsExtractor {

    private static final String ATTRIBUTE_KEY = "value";

    static Map<String, String> extractInputElements(Element form) {
        Map<String, String> params = new LinkedHashMap<>();
        Elements inputElements = form.getElementsByTag("input");
        inputElements.stream()
                .filter(element -> isDataElement(element) || element.attr("name").length() > 0)
                .forEach(element -> params.put(element.attr("name"), extractValue(element)));
        return params;
    }

    static Map<String, String> extractSelectElements(Element form) {
        Map<String, String> params = new LinkedHashMap<>();
        Elements selectElements = form.getElementsByTag("select");
        selectElements.stream()
                .filter(element -> element.attr("name").length() > 0)
                .forEach(
                        element -> {
                            Elements options = element.getElementsByAttribute("selected");
                            options.addAll(element.getElementsByAttribute("checked"));
                            params.put(element.attr("name"), extractValue(element));
                        });
        return params;
    }

    private static String extractValue(Element element) {
        if (isnRadioOrCheckboxElement(element)) {
            return ((element.hasAttr("checked") || element.hasAttr("selected"))
                            && element.hasAttr(ATTRIBUTE_KEY))
                    ? element.attr(ATTRIBUTE_KEY)
                    : "";
        } else {
            return element.hasAttr(ATTRIBUTE_KEY) ? element.attr(ATTRIBUTE_KEY) : "";
        }
    }

    private static boolean isDataElement(Element element) {
        String type = element.attr("type");
        return !("button".equalsIgnoreCase(type)
                || "submit".equalsIgnoreCase(type)
                || "image".equalsIgnoreCase(type));
    }

    private static boolean isnRadioOrCheckboxElement(Element element) {
        String type = element.attr("type");
        return "radio".equalsIgnoreCase(type) || "checkbox".equalsIgnoreCase(type);
    }
}
