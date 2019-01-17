package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.core.Amount;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IngAtCreditCardParser {
    private final Document doc;

    public IngAtCreditCardParser(Document doc) {
        this.doc = doc;
    }

    public IngAtCreditCardParser(String htmlText) {
        this(Jsoup.parse(htmlText));
    }

    public Document getDoc() {
        return doc;
    }

    /** Converts e.g. "€ 1.234,56" -> 1234.56 */
    private static double valueFromAmountString(final String amountString) {
        return Double.parseDouble(
                amountString
                        .replaceAll("\\s+", "")
                        .replace("€", "")
                        .replace(".", "")
                        .replace(",", "."));
    }

    public Amount getSaldo() {
        final Element e = doc.select(String.format("h3:contains(%s)", "saldo")).first();
        if (!Objects.isNull(e)) {
            final Element p = e.parent().parent();
            if (!Objects.isNull(p)) {
                final Element b = p.select("span:matches(€?\\s*\\-?\\d)").first();
                return IngAtAmmountParser.toAmount(b.text());
            }
        }
        return null;
    }

    public Amount getAvailableCredit() {
        final Element e = doc.select(String.format("h3:contains(%s)", "Verfügbar")).first();
        if (!Objects.isNull(e)) {
            final Element p = e.parent().parent();
            if (!Objects.isNull(p)) {
                final Element b = p.select("span:matches(€?\\s*\\-?\\d)").first();
                return new Amount("EUR", valueFromAmountString(b.text()));
            }
        }
        return null;
    }

    public String getCardNumber() {
        final Element e = doc.select(String.format("dt:contains(%s)", "Kartennummer")).first();
        if (Objects.nonNull(e)) {
            final Element f = e.parent();
            if (Objects.nonNull(f)) {
                final Element g =
                        f.select(
                               String.format(
                                        "dd:matches(%s)",
                                        "\\*\\*\\*\\* \\*\\*\\*\\* \\*\\*\\*\\*"))
                                .first();
                return g.text();
            }
        }
        return "";
    }

    public String getConnectedAccountNumber() {
        final Element e = doc.select(String.format("dt:contains(%s)", "Verrechnungskonto")).first();
        if (Objects.nonNull(e)) {
            final Element f = e.parent();
            if (Objects.nonNull(f)) {
                final Element g =
                        f.select(
                                String.format(
                                        "dd:matches(%s)",
                                        ".{2}\\d{2} \\d{4} \\d{4} \\d{4} \\d{4}"))
                        .first();
                if (Objects.nonNull(g)) {
                    return cleanAccountNumber(g.text());
                }
            }
        }
        return "";
    }

    public String getUniqueIdentifier() {
        String uniqueId = getCardNumber() + getConnectedAccountNumber();
        return Hash.sha256AsHex(uniqueId);
    }

    public String getIdentifier() {
        String result = "";
        final Element e = doc.select(String.format("h1:contains(%s)", "Kreditkarte")).first();
        if (Objects.isNull(e)) {
            return result;
        }
        final Element f = e.parent();
        if (Objects.isNull(f)) {
            return result;
        }
        String formatted = String.format("span:matches(%s)", "\\*\\*\\*\\* \\*\\*\\*\\* \\*\\*\\*\\*");
        final Element g = e.select(formatted).first();
        if (Objects.isNull(g)) {
            return result;
        }
        return g.text();
    }

    private String cleanAccountNumber(final String accountString) {
        String result = accountString;
        Pattern pattern = Pattern.compile("(( .{4}){5})");
        Matcher matcher = pattern.matcher(accountString);
        if (matcher.find()) {
            result = matcher.group(1).trim();
        }
        return result;
    }
}
