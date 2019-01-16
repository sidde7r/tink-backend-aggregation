package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class IngAtDateRangeTransactionsParser {
    private Document ajaxDoc;
    private Document doc;

    public IngAtDateRangeTransactionsParser(Document ajaxDoc) {
        this.ajaxDoc = doc;
        this.doc = Jsoup.parse(getHtmlText(ajaxDoc));
    }

    public IngAtDateRangeTransactionsParser(String ajaxText) {
        this(Jsoup.parse(ajaxText));
    }

    private String getHtmlText(Document ajaxDoc) {
        final Elements components = ajaxDoc.getElementsByTag("component");
        Element f = components.first();
        String text = f.text();
        return text;
    }

    private List<Transaction> parse() throws ParseException {
        final List<Transaction> res = new ArrayList<>();
        final SimpleDateFormat dateParser = new SimpleDateFormat("dd.MM.yyyy");
        final Elements tables = doc.select("table[class=transactions-table__entry-table]");
        for (Element t : tables) {
            final String description = t.select("p[class=transaction-name__text]").first().text();
            final String date = t.select("p[class=transaction-name__date]").first().text();
            final String amount =
                    t.select("p[class=transaction-name__amount]")
                            .first()
                            .text()
                            .replaceAll("\\s+", "")
                            .replace("€", "")
                            .replace(".", "")
                            .replace(",", ".");
            final Transaction transaction =
                    new Transaction.Builder()
                            .setAmount(new Amount("EUR", Double.parseDouble(amount)))
                            .setDate(dateParser.parse(date))
                            .setDescription(description)
                            .build();
            res.add(transaction);
        }
        return res;
    }

    public List<Transaction> getTransactions() {
        try {
            return parse();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to read transactions", e);
        }
    }
}
