package se.tink.backend.common.utils.giro.lookup;

import java.util.Optional;
import com.google.common.base.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.sun.jersey.api.client.Client;
import se.tink.libraries.net.BasicJerseyClientFactory;
import se.tink.libraries.giro.validation.LuhnCheck;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.backend.utils.StringUtils;

import javax.ws.rs.core.MediaType;
import java.util.Objects;

public class BankGiroCrawler {
    private static final String URL = "http://www.bankgirot.se/sok-bankgironummer/?bgnr=";
    private static final String NO_HITS_FOUND_PATTERN = "(0)";
    private final Client client;

    public BankGiroCrawler() {
        client = new BasicJerseyClientFactory().createThreadSafeCookieClient();
    }

    /** This constructor are only used by the unit tests,
     *  Do NOT inject a client when using this package
     *  from production files
     */
    public BankGiroCrawler(Client client) {
        this.client = client;
    }

    public Optional<AccountIdentifier> find(String accountNumber) {
        Optional<String> parsedAccountNumber = parseAndValidate(accountNumber);

        if (parsedAccountNumber.isPresent()) {
            accountNumber = parsedAccountNumber.get();

            Document doc = getPageContent(accountNumber);
            Optional<String> name = findNameIn(doc, accountNumber);

            return createIdentifierFor(accountNumber, name);
        }

        return Optional.empty();
    }

    private Optional<String> findNameIn(Document doc, String accountNumber) {
        if (!doc.select(".num-of-hits").text().equals(NO_HITS_FOUND_PATTERN)) {
            Elements rows = doc.select(".search-result").select(".result-container");
            Element resultContainer = null;

            if (rows.size() > 0) {
                for (Element row : rows) {
                    String giroNumber = row.getElementsByTag("ul").get(2)
                            .getElementsByTag("li").get(1).text()
                            .replace("-", "");

                    if (Objects.equals(giroNumber, accountNumber)) {
                        resultContainer = row;
                        break;
                    }
                }
            }

            if (resultContainer != null) {
                String name = resultContainer.select(".title").get(0)
                        .text()
                        .trim()
                        .replaceAll("[^a-öA-Ö0-9]$", "");

                return Optional.of(StringUtils.formatHuman(name));
            }
        }

        return Optional.empty();
    }

    public Document getPageContent(String accountNumber) {
        String response = createClientRequest(accountNumber);

        return Jsoup.parse(response);
    }

    private String createClientRequest(String accountNumber) {
        return client.resource(URL + accountNumber)
                .accept(MediaType.TEXT_HTML)
                .get(String.class);
    }

    private Optional<AccountIdentifier> createIdentifierFor(String accountNumber, Optional<String> name) {
        AccountIdentifier identifier = null;

        if (name.isPresent()) {
            identifier = new BankGiroIdentifier(accountNumber);
            identifier.setName(name.get());
        }

        return Optional.ofNullable(identifier);
    }

    private Optional<String> parseAndValidate(String accountNumber) {
        if (!Strings.isNullOrEmpty(accountNumber)) {
            accountNumber = accountNumber.replace("-", "");

            if ((accountNumber.length() == 7 || accountNumber.length() == 8) &&
                    LuhnCheck.isLastCharCorrectLuhnMod10Check(accountNumber)) {

                return Optional.of(accountNumber);
            }
        }

        return Optional.empty();
    }
}
