package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class IngAtValidationSignatureParser {
    private Document doc;

    public IngAtValidationSignatureParser(Document doc) {
        this.doc = doc;
    }

    public IngAtValidationSignatureParser(String htmlText) {
        this(Jsoup.parse(htmlText));
    }

    public Document getDocument() {
        return doc;
    }

    public Optional<String> getValidationSignature() {
        try {
            final String s = doc.select("form[class=resmargin_100]").select("input[name=DIBA_SEC_FORM_VALIDATION_SIGN]").first().attributes().get("value");
            return Optional.ofNullable(s);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}