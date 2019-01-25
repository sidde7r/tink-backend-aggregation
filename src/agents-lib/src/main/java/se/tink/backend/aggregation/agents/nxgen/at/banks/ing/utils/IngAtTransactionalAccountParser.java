package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.libraries.amount.Amount;

import java.util.Optional;

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

    public Amount getAmount() {
        final Element e = doc.select(String.format("h3:contains(%s)", "Kontostand")).first();
        final Element p = e.parent().parent();
        final Element b = p.select("span:matches(€?\\s*\\-?\\d)").first();

        return IngAtAmmountParser.toAmount(b.text());
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
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public String getIban() {
        return getAccountIdentifier("IBAN");
    }
}
