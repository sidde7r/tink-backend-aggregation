package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import com.google.common.base.Preconditions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IngAtOpeningDateParser {
    private final Document doc;

    public IngAtOpeningDateParser(final String html) {
        doc = Jsoup.parse(html);
    }

    public String getOpeningDate() {
        // Shown when you attempt to download a CSV file with a date range that starts before the
        // account was opened.
        // Error text of the form:
        // "Bitte geben Sie ein Datum seit der Kontoeröffnung (09.12.2016) ein."

        final Elements components = doc.select("component");
        Preconditions.checkState(!components.isEmpty());
        final String componentHtml = components.first().text();

        final Document componentDoc = Jsoup.parse(componentHtml);

        final Elements errorElements = componentDoc.select("div[class=error obr_error]");
        Preconditions.checkState(!errorElements.isEmpty());
        final Elements pElements = errorElements.select("p:contains(Kontoeröffnung)");
        Preconditions.checkState(!pElements.isEmpty());

        final String text = pElements.last().text();
        final Pattern pattern = Pattern.compile("\\d\\d?\\.\\d\\d?\\.\\d\\d\\d\\d");
        final Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        } else {
            throw new IllegalStateException();
        }
    }
}
