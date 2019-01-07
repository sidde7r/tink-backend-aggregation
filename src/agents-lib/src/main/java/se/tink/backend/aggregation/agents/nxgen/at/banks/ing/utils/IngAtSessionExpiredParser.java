package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtConstants;

public final class IngAtSessionExpiredParser {
    private final Document doc;

    private IngAtSessionExpiredParser(final Document doc) {
        this.doc = doc;
    }

    public IngAtSessionExpiredParser(final String htmlText) {
        this(Jsoup.parse(htmlText));
    }

    public boolean isSessionExpired() {
        final String contentText = doc.body().select("div[class=content]").text();
        return contentText.toLowerCase().contains(IngAtConstants.Messages.SESSION_EXPIRED);
    }
}
