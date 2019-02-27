package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.backend.aggregation.nxgen.http.Form;

public class IngAtPasswordFormParser {
    private Document doc;

    public IngAtPasswordFormParser(Document doc) {
        this.doc = doc;
    }

    public IngAtPasswordFormParser(String htmlText) {
        this(Jsoup.parse(htmlText));
    }

    public Document getDocument() {
        return doc;
    }

    public Form getForm() {
        Elements passwordForm = doc.select("form[action*=password_form");
        Form.Builder formBuilder = Form.builder();
        for (Element e : doc.select("input")) {
            if (!e.attr("type").equalsIgnoreCase("submit")) {
                formBuilder.put(e.attr("name"), e.attr("value"));
            }
        }
        return formBuilder.build();
    }
}
