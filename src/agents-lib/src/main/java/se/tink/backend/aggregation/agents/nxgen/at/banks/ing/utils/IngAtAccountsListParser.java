package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.agents.rpc.AccountTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IngAtAccountsListParser {
    private static Logger logger = LoggerFactory.getLogger(IngAtAccountsListParser.class);

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

    public Optional<String> getAccountHolder() {
        final List<String> greeting =
                doc.select("small[class=title__subheadline]")
                        .select("span")
                        .stream()
                        .map(Element::text)
                        .collect(Collectors.toList());

        // The greeting consist of three segments, roughly: ["Hi", "Herr", "Sebastian Olsson"]
        if (greeting.size() != 3) {
            logger.warn("Could not extract account holder");
            return Optional.empty();
        }
        return Optional.ofNullable(greeting.get(2));
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
        private String accountName;
        private String id;
        private String currency;
        private double balance;

        private AccountSummary(Elements columns) {
            final Element link = columns.get(0);
            this.type = textToAccountType(link.text());
            this.link = link.getElementsByTag("a").attr("href");
            this.accountName =
                    Optional.ofNullable(link.getElementsByTag("span").last()).orElse(null).text();
            this.id = columns.get(2).text();
            this.currency = "EUR";
            String balance = columns.get(3).text();
            if (balance.endsWith("€")) {
                balance = balance.substring(0, balance.length() - 1).trim();
            }
            this.balance = Double.parseDouble(balance.replace(".", "").replace(',', '.'));
        }

        public String getType() {
            return type;
        }

        public String getLink() {
            return link;
        }

        public String getAccountName() {
            return accountName;
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
            return String.format(
                    "AccountSummary(type=%s, link=%s, accountName=%s, id=%s, currency=%s, balance=%s)",
                    type, link, accountName, id, currency, balance);
        }
    }
}
