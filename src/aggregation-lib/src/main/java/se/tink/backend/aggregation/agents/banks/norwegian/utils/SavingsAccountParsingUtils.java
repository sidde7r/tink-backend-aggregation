package se.tink.backend.aggregation.agents.banks.norwegian.utils;

import java.util.Optional;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.tink.backend.utils.StringUtils;

public class SavingsAccountParsingUtils {

    public static Double parseSavingsAccountBalance(String htmlContent) {
        Elements tdElements = Jsoup.parse(htmlContent).select(".page").select("td");
        Iterator<Element> iterator = tdElements.iterator();
        String balance = null;
        while (iterator.hasNext()) {
            Element tdElement = iterator.next();
            if (tdElement.text().toLowerCase().contains("belopp")) {
                balance = iterator.next().text();
                break;
            }
        }

        // Means that the account has been created recently
        if (balance == null || balance.isEmpty() || balance.equalsIgnoreCase("Inte tillgänglig")) {
            return 0D;
        }

        return StringUtils.parseAmount(balance);
    }

    public static Optional<String> parseSavingsAccountNumber(String htmlContent) {
        Elements elements = Jsoup.parse(htmlContent).select(".page");

        Pattern pattern = Pattern.compile("Overview\\?accountNumber=(.+?)\"");
        Matcher matcher = pattern.matcher(elements.html());

        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(matcher.group(1));
    }
}