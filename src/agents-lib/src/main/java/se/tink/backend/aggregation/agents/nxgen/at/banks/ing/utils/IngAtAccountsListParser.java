package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class IngAtAccountsListParser {
    private Document doc;

    public IngAtAccountsListParser(Document doc) {
        this.doc = doc;
    }

    public IngAtAccountsListParser(String htmlText) {
        this(Jsoup.parse(htmlText));
    }

    private static String textToAccountType(String s) {
        s = s.toLowerCase();
        switch (s) {
        case "girokonto":
            return AccountTypes.CHECKING.toString();
        case "direkt-sparkonto":
            return AccountTypes.SAVINGS.toString();
        default:
            if (s.contains("kreditkarte")) {
                return AccountTypes.CREDIT_CARD.toString();
            } else {
                return AccountTypes.OTHER.toString();
            }
        }
    }

    public String getAccountHolder() {
        return doc.select("small[class=title__subheadline]").select("span").last().text();
    }

    private AccountSummary parseAccount(Element r) {
        Elements columns = r.getElementsByTag("td");
        AccountSummary res = new AccountSummary(columns);
        return res;
    }

    public List<AccountSummary> getAccountsSummary() {
        List<AccountSummary> res = new ArrayList<>();
        for (Element r : doc.select("table").select("tr[class=kontentablerow]")) {
            res.add(parseAccount(r));
        }
        return res;
    }

    @JsonObject
    public static class AccountSummary {
        private String type;
        private String link;
        private String id;
        private String currency;
        private double balance;

        private AccountSummary(Elements columns) {
            final Element link = columns.get(0);
            this.type = textToAccountType(link.text());
            this.link = link.getElementsByTag("a").attr("href");
            this.id = columns.get(2).text();
            this.currency = "EUR";
            String balance = columns.get(3).text();
            if (balance.endsWith("€")) {
                balance = balance.substring(0, balance.length() - 1).trim();
            }
            this.balance = Double.parseDouble(balance
                    .replace(".", "")
                    .replace(',', '.'));
        }

        public String getType() {
            return type;
        }

        public String getLink() {
            return link;
        }

        public String getId() {
            return id;
        }

        public String getCurrency() {
            return currency;
        }

        public double getBalance() {
            return balance;
        }

        @Override
        public String toString() {
            return String.format("AccountSummary(type=%s, link=%s, id=%s, currency=%s, balance=%s)",
                    type, link, id, currency, balance);
        }
    }
}