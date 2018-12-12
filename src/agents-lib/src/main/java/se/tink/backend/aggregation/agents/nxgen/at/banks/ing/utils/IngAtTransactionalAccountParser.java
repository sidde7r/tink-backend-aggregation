package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import java.util.Optional;
import org.apache.zookeeper.Op;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.core.Amount;

public class IngAtTransactionalAccountParser {

    private final Document doc;

    public IngAtTransactionalAccountParser(Document doc) {
        this.doc = doc;
    }

    public IngAtTransactionalAccountParser(String htmlText) {
        this(Jsoup.parse(htmlText));
    }

    public Document getDoc() {
        return doc;
    }

    private String getTotal(final String type) {
        final Element e = doc.select(String.format("h3:contains(%s)", type)).first();
        final Element p = e.parent().parent();
        final Element b = p.select("span:matches(€?\\s*\\-?\\d)").first();
        final String result = b.text()
                .replaceAll("\\s+", "")
                .replace("€", "")
                .replace(".", "")
                .replace(",", ".");
        return result;
    }

    public Amount getAmount() {
        return new Amount("EUR", Double.parseDouble(getTotal("Kontostand")));
    }

    private String getAccountIdentifier(final String type) {
        final Element e = doc.select(String.format("dt:contains(%s)", type)).first();
        final Element p = e.parent();
        final String iban = p.getElementsByTag("dd").first().text().replaceAll("\\s+", "");
        return iban;
    }

    public Optional<String> getBic() {
        try {
            return Optional.of(getAccountIdentifier("BIC"));
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }

    public String getIban() {
        return getAccountIdentifier("IBAN");
    }
}
