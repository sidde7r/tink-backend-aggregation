package se.tink.backend.common.utils.giro.lookup;

import com.google.common.base.Charsets;
import java.util.Optional;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.IOException;
import java.io.InputStreamReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import se.tink.libraries.net.BasicJerseyClientFactory;
import com.sun.jersey.api.client.Client;
import se.tink.libraries.giro.validation.LuhnCheck;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

public class PlusGiroCrawler {
    private static final LogUtils log = new LogUtils(PlusGiroCrawler.class);
    private static final String URL = "https://kontoutdrag.plusgirot.se/ku/sokko002";
    private final Client client;

    public PlusGiroCrawler() {
        client = new BasicJerseyClientFactory().createCookieClientWithoutSSL();
    }

    /** This constructor are only used by the unit tests,
     *  Do NOT inject a client when using this package
     *  from production files
     */
    public PlusGiroCrawler(final Client client) {
        this.client = client;
    }

    public Optional<AccountIdentifier> find(String accountNumber) {
        Optional<String> parsedAccountNumber = parseAndValidate(accountNumber);

        if (parsedAccountNumber.isPresent()) {
            accountNumber = parsedAccountNumber.get();

            try {
                Document doc = getPageContent(accountNumber);
                String name = findNameIn(doc);

                return createIdentifierFor(accountNumber, name);
            } catch (Exception e) {
                log.error("Was not able to lookup plusgiro: " + accountNumber, e);
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private String findNameIn(Document doc) {
        Elements rows = doc.getElementsByTag("table").get(1).getElementsByTag("tr");

        if (rows.size() > 2) {
            String rawText = rows.get(2).getElementsByTag("td").get(0)
                    .getElementsByTag("span").get(1)
                    .textNodes().get(0)
                    .getWholeText()
                    .trim();

            String name = rawText.split("\n")[0].replaceAll("[^a-öA-Ö0-9]$", "");

            return StringUtils.formatHuman(name);
        }

        return null;
    }

    private Document getPageContent(String accountNumber) throws IOException {
        MultivaluedMap formData = createFormData(accountNumber);

        return Jsoup.parse(createClientRequest(formData));
    }

    private String createClientRequest(MultivaluedMap formData) throws IOException {

        ClientResponse response = client.resource(URL)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.TEXT_HTML).post(ClientResponse.class, formData);

        return CharStreams.toString(new InputStreamReader(response.getEntityInputStream(), Charsets.ISO_8859_1));
    }

    private MultivaluedMap createFormData(String accountNumber) {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add("SO_KTO", accountNumber);

        return formData;
    }

    private Optional<AccountIdentifier> createIdentifierFor(String accountNumber, String name) {
        AccountIdentifier identifier = null;

        if (!Strings.isNullOrEmpty(name)) {
            identifier = new PlusGiroIdentifier(accountNumber);
            identifier.setName(name);
        }

        return Optional.ofNullable(identifier);
    }

    private Optional<String> parseAndValidate(String accountNumber) {
        if (!Strings.isNullOrEmpty(accountNumber)) {
            accountNumber = accountNumber.replace("-", "");

            if ((accountNumber.length() >= 2 && accountNumber.length() <= 8) &&
                    LuhnCheck.isLastCharCorrectLuhnMod10Check(accountNumber)) {

                return Optional.of(accountNumber);
            }
        }

        return Optional.empty();
    }
}
