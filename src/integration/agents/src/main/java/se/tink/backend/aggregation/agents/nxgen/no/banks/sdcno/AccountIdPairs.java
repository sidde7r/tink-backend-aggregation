package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import com.google.common.base.Strings;
import java.util.Set;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import se.tink.libraries.pair.Pair;

class AccountIdPairs {
    private final Document document;

    AccountIdPairs(final String webpage) {
        document = Jsoup.parse(webpage);
    }

    Set<Pair<String, String>> extractAll() {
        return document.select("a.list__anchor").stream()
                .map(e -> new Pair<>(e.attr("data-id"), e.attr("data-idkey")))
                .filter(p -> !Strings.isNullOrEmpty(p.first) && !Strings.isNullOrEmpty(p.second))
                .collect(Collectors.toSet());
    }
}
