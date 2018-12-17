package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IngAtAntiCacheParser {
    private Document doc;

    private String url;
    private Integer pageNumber;
    private Long antiCache;

    public IngAtAntiCacheParser(Document doc) {
        this.doc = doc;
    }

    public IngAtAntiCacheParser(String htmlText) {
        this(Jsoup.parse(htmlText));
    }

    public Document getDocument() {
        return doc;
    }

    public String getUrl() {
        if (url == null) {
            parse();
        }
        return url;
    }

    public Integer getPageNumber() {
        if (pageNumber == null) {
            parse();
        }
        return pageNumber;
    }

    private void parse() {
        final String s = doc.select("evaluate").first().text();
        final Pattern pattern =
                Pattern.compile(
                        "window\\.location\\.href='(.*page\\?(\\d+).*antiCache\\s*=\\s*([0-9]+))'");
        final Matcher m = pattern.matcher(s);
        if (m.find()) {
            url = m.group(1);
            pageNumber = Integer.parseInt(m.group(2));
            antiCache = Long.parseLong(m.group(3));
        } else {
            throw new IllegalStateException();
        }
    }
}
