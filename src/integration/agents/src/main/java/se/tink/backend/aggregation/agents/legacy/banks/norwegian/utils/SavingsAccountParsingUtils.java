package se.tink.backend.aggregation.agents.banks.norwegian.utils;

import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import se.tink.libraries.strings.StringUtils;

public class SavingsAccountParsingUtils {

    public static Double parseSavingsAccountBalance(String htmlContent) {
        Elements elements = Jsoup.parse(htmlContent).select("div.grid-u-1-2");

        if (elements.size() == 0) {
            return 0D;
        }

        List<TextNode> textNodes = elements.get(1).textNodes();

        if (textNodes.size() == 0) {
            return 0D;
        }

        String balance = textNodes.get(0).text();

        // Means that the account has been created recently
        if (balance == null || balance.isEmpty() || balance.equalsIgnoreCase("Inte tillgänglig")) {
            return 0D;
        }

        return StringUtils.parseAmount(balance);
    }

    public static Optional<String> parseSavingsAccountNumber(String htmlContent) {
        Elements elements = Jsoup.parse(htmlContent).select("div.card__data--label");

        // Return Optional.empty if user doesn't have a savings account
        if (elements.size() == 0) {
            return Optional.empty();
        }

        return Optional.of(elements.get(0).text());
    }
}
