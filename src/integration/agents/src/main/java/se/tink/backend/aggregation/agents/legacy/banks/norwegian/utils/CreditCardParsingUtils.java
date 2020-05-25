package se.tink.backend.aggregation.agents.banks.norwegian.utils;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.libraries.strings.StringUtils;

public class CreditCardParsingUtils {

    /** Parses the balance from Norwegian card balance page */
    public static Double parseBalance(String htmlContent) {
        Document document = Jsoup.parse(htmlContent);

        Double creditLimit = StringUtils.parseAmount(getUnescapedAmountString(document, 1));

        Double leftToSpend = StringUtils.parseAmount(getUnescapedAmountString(document, 5));

        return leftToSpend - creditLimit;
    }

    private static String getUnescapedAmountString(Document document, int amountStringIndex) {
        String escapedAmount =
                document.select("div.grid")
                        .get(0)
                        .select("div.grid-u-1-2")
                        .get(amountStringIndex)
                        .text();

        return StringEscapeUtils.unescapeHtml4(escapedAmount);
    }

    public static class AccountNotFoundException extends Exception {
        private static final long serialVersionUID = 2911080861317215659L;

        public AccountNotFoundException(String message) {
            super(message);
        }
    }

    public static Optional<String> parseAccountName(String htmlContent) {
        Elements elements =
                Jsoup.parse(htmlContent)
                        .select("div.contactinfo-address.contactinfo-address--current");

        if (elements.size() == 0 || elements.get(0).select("p").size() == 0) {
            throw new NoSuchElementException("Could not find identity data. HTML changed?");
        }

        return Optional.of(elements.get(0).select("p").first().text().trim());
    }

    public static Optional<String> parseTransactionalAccountNumber(String htmlContent) {
        Element script = Jsoup.parse(htmlContent);

        Pattern pattern = Pattern.compile("\"accountNo\":\"(.+?)\"");
        Matcher matcher = pattern.matcher(script.html());

        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(matcher.group(1));
    }
}
