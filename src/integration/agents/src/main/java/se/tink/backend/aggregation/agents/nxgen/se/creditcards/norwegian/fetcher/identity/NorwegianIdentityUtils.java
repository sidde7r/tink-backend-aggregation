package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.identity;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class NorwegianIdentityUtils {

    public static Optional<String> parseAccountName(String htmlContent) {
        Elements elements =
                Jsoup.parse(htmlContent)
                        .select("div.contactinfo-address.contactinfo-address--current");

        if (elements.size() == 0 || elements.get(0).select("p").size() == 0) {
            throw new NoSuchElementException("Could not find identity data. HTML changed?");
        }

        return Optional.of(elements.get(0).select("p").first().text().trim());
    }
}
