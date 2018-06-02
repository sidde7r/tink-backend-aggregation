package se.tink.backend.aggregation.agents.banks.norwegian.utils;

import java.util.Optional;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.backend.utils.StringUtils;

public class CreditCardParsingUtils {

    /**
     * Parses the balance from Norwegian card balance page
     */
    public static Double parseBalance(String htmlContent) {
        Document document = Jsoup.parse(htmlContent);

        Double creditCeiling = StringUtils.parseAmount(document
                .select(".page")
                .select(".data-table")
                .select(".data-table-value")
                .get(0)
                .text());

        Double leftToSpend = StringUtils.parseAmount(document
                .select(".page")
                .select(".data-table")
                .select(".data-table-value")
                .get(2)
                .text());

        return leftToSpend - creditCeiling;
    }

    public static class AccountNotFoundException extends Exception {
        private static final long serialVersionUID = 2911080861317215659L;

        public AccountNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Parses the account number from Norwegian account details page
     * 
     * @throws AccountNotFoundException
     *             if no account could be extracted
     */
    public static String parseAccountNumber(String htmlContent) throws AccountNotFoundException {
        Elements elements = Jsoup.parse(htmlContent)
                .select(".page > table > tbody > tr");
        if (elements.size() == 0) {
            throw new AccountNotFoundException("Could not extract account number.");
        }

        return elements
                .get(0)
                .select("td")
                .first()
                .text();
    }

    public static Optional<String> parseTransactionalAccountNumber(String htmlContent) {
        Element script = Jsoup.parse(htmlContent);

        Pattern pattern = Pattern.compile("TransactionViewModel\\(\"(.+?)\"");
        Matcher matcher = pattern.matcher(script.html());

        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(matcher.group(1));
    }

    /**
     * Parses list of pages that could be collected
     */
    public static List<Integer> parsePages(String htmlContent) {

        List<Integer> result = Lists.newArrayList();

        Elements selects = Jsoup.parse(htmlContent).select("#filter").first().children();

        for (Element select : selects) {
            result.add(Integer.parseInt(select.val()));
        }

        return result;
    }

}
