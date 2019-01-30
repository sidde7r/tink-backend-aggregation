package se.tink.backend.aggregation.agents.other;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.readers.CharacterEncodedMessageBodyReader;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class CSNAgent extends AbstractAgent implements DeprecatedRefreshExecutor {

    private static final File TRUST_STORE = new File("data/security/csn.truststore");
    private static final char[] TRUST_STORE_PASSWORD = "changeme".toCharArray();

    private final Pattern reBalance = Pattern
            .compile(
                    "aktuellStudieskuld\\.do\\?metod=init&(?:amp;)?SpecNr=(\\d{1,})\">([^<]+)</a>\\s*</td>\\s*<td[^>]+>([^<]+)</td>",
                    Pattern.CASE_INSENSITIVE);

    private static final String NO_LOAN = "det finns inga uppgifter om din studieskuld eller ditt återkrav i mina sidor";

    private static final Joiner REGEXP_OR_JOINER = Joiner.on("|");
    private final TinkHttpClient client;
    private final Credentials credentials;
    private String loginResponse;
    private boolean hasRefreshed = false;

    public CSNAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context);

        this.credentials = request.getCredentials();

        this.client = new TinkHttpClient(context, request.getCredentials());
        //When Java trusted certificates are updated this is probably no longer necessary:
        this.client.loadTrustMaterial(loadCustomTrustStore(), null);
        this.client.addMessageReader(new CharacterEncodedMessageBodyReader(StandardCharsets.ISO_8859_1));
    }

    private KeyStore loadCustomTrustStore() {
        if (!TRUST_STORE.exists()) {
            throw new IllegalStateException("Cannot find necessary trust store file. Cannot complete SSL handshake "
                    + "without it.");
        }
        try (FileInputStream fileInputStream = new FileInputStream(TRUST_STORE)) {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(fileInputStream, TRUST_STORE_PASSWORD);
            return trustStore;
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void refresh() throws Exception {
        // The refresh command will call refresh multiple times.
        // This check ensures the refresh only runs once.
        if (hasRefreshed) {
            return;
        }
        hasRefreshed = true;

        Matcher matcher = this.reBalance.matcher(this.loginResponse);

        if (!matcher.find()) {
            if (this.loginResponse.toLowerCase().contains(NO_LOAN)) {
                // No loans on this credential
                return;
            }

            throw new IllegalStateException("CSN error: Could not fetch loan balance.");
        }

        do {
            String description = (matcher.group(2)).trim();
            String id = matcher.group(1).trim();
            double balance = -parseAmount(matcher.group(3).replace(",", ""));

            Account account = new Account();

            account.setName("Studielån");
            account.setBankId(String.format("%s: %s", this.credentials.getUsername(), description));
            account.setBalance(balance);
            account.setType(AccountTypes.LOAN);

            Preconditions.checkState(
                    Preconditions.checkNotNull(account.getBankId()).matches(
                            REGEXP_OR_JOINER.join("\\d{12}: Lån efter 30 juni 2001 \\(annuitetslån\\)",
                                    "\\d{12}: Återkrav [^ ]+ halvåret [0-9]{4} lån [0-9]+",
                                    "\\d{12}: Lån 1 januari 1989-30 juni 2001 \\(studielån\\)",
                                    "\\d{12}: Lån före 1989 \\(studiemedel\\)")),
                    "Unexpected account.bankid '%s'. Reformatted?", account.getBankId());

            this.financialDataCacher.cacheAccount(account);
        } while (matcher.find());
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {

        this.client.addPersistentHeader("Referer", "https://tjanster.csn.se/bas/");

        this.loginResponse = get("https://tjanster.csn.se/bas/inloggning/pinkod.do");

        Preconditions.checkState(this.loginResponse.contains("Fyll i ditt personnummer samt din personliga kod"));

        post("https://tjanster.csn.se/bas/inloggning/Pinkod.do", new LoginForm());

        this.loginResponse = get("https://tjanster.csn.se/aterbetalning/hurStorArMinSkuld/aktuellStudieskuld.do");

        if (!this.loginResponse.contains("Inloggad&nbsp;som")) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        return true;
    }

    private String get(String url) {
        return this.client.request(url).get(String.class);
    }

    private void post(String url, MultivaluedMap postData) {
        this.client.request(url)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(postData);
    }

    @Override
    public void logout() throws Exception {
        // NOP
    }

    private class LoginForm extends MultivaluedMapImpl {
        LoginForm() {
            this.add("metod", "validerapinkod");
            this.add("pnr", CSNAgent.this.credentials.getUsername().substring(2));
            this.add("pinkod", CSNAgent.this.credentials.getPassword());
        }
    }
}
