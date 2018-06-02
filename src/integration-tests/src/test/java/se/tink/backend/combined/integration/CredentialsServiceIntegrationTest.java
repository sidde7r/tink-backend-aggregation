package se.tink.backend.combined.integration;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.sun.jersey.api.client.ClientResponse.Status;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Field;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

/**
 * TODO this is a unit test
 */
public class CredentialsServiceIntegrationTest extends AbstractServiceIntegrationTest {

    private ImmutableMap<String, Provider> providersByName = null;
    protected User user;
    private CredentialsRepository credentialsRepository;

    @Before
    public void setUp() throws Exception {
        user = null;
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
    }

    @After
    public void tearDown() throws Exception {
        if (user != null) {
            deleteUser(user);
        }
    }

    @Test
    public void testCreateDemoCredentials() throws Exception {
        user = registerTestUserWithDemoCredentialsAndData();
    }

    protected void testHybridAgent(String username, String password, String providerName) throws Exception {
        User user = registerUser(randomUsername(), "testing", createUserProfile());

        Credentials c1 = new Credentials();
        c1.setProviderName(providerName);
        c1.setType(CredentialsTypes.PASSWORD);
        c1.setUsername(username);
        c1.setPassword(password);

        Credentials c2 = serviceFactory.getCredentialsService().create(
                new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user),
                null,
                c1,
                Collections.emptySet());

        Thread.sleep(5000);
        waitForRefresh(user);

        List<Transaction> ts1 = serviceFactory.getTransactionService().list(user, null, null, null, 0, 0, null, null);

        serviceFactory.getCredentialsService().refresh(
                new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user),
                c2.getId(),
                null);

        Thread.sleep(5000);
        waitForRefresh(user);

        List<Transaction> ts2 = serviceFactory.getTransactionService().list(user, null, null, null, 0, 0, null, null);

        Assert.assertEquals(ts1.size(), ts2.size());
    }

    @Test
    public void testConflictingCredentials() throws Exception {
        User user = registerUser(randomUsername(), "testing", createUserProfile());

        Credentials credentials = new Credentials();
        credentials.setProviderName("demo");
        credentials.setUsername("anv1");
        credentials.setPassword("demo");

        serviceFactory.getCredentialsService().create(
                new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user),
                null,
                credentials,
                Collections.emptySet());

        Thread.sleep(2500);

        waitForRefresh(user);

        boolean caughtException = false;

        try {
            serviceFactory.getCredentialsService().create(
                    new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user),
                    null,
                    credentials,
                    Collections.emptySet());
        } catch (WebApplicationException e) {
            Assert.assertEquals(e.getResponse().getStatus(), Status.CONFLICT.getStatusCode());
            caughtException = true;
        }

        Assert.assertTrue(caughtException);
    }

    @Test
    public void testConflictingCredentialsDifferentProviders() throws Exception {
        User user = registerUser(randomUsername(), "testing", createUserProfile());

        Credentials credentials = new Credentials();
        credentials.setProviderName("lansforsakringar");
        credentials.setUsername("201212121212");
        credentials.setPassword("1212");

        serviceFactory.getCredentialsService().create(
                new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user),
                null,
                credentials,
                Collections.emptySet());

        Thread.sleep(2500);

        waitForRefresh(user);

        boolean caughtException = false;

        try {
            Credentials credentials2 = new Credentials();
            credentials2.setProviderName("lansforsakringar-bankid");
            credentials2.setUsername("201212121212");
            credentials2.setPassword("1212");

            serviceFactory.getCredentialsService().create(
                    new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user),
                    null,
                    credentials2,
                    Collections.emptySet());
        } catch (WebApplicationException e) {
            Assert.assertEquals(e.getResponse().getStatus(), Status.CONFLICT.getStatusCode());
            caughtException = true;
        }

        Assert.assertTrue(caughtException);
    }

    @Test
    public void testStatusNotNullable() throws Exception {
        User user = registerUser(randomUsername(), "testing", createUserProfile());

            Credentials credentials = new Credentials();
            credentials.setProviderName("demo");
            credentials.setUsername("anv1");
            credentials.setPassword("demo");
            credentials.setStatus(null);

            CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);

            // Checking if @Column(nullable=false) yields validation error. It doesn't. Exception will only be thrown if
            // `status` isn't nullable in the MySQL table.
            try {
                credentialsRepository.save(credentials);
            } catch (DataIntegrityViolationException | ConstraintViolationException e) {
                // Expected
                return;
            }
            Assert.fail("Expected data integrity fail error.");

    }

    @Test
    public void testUpdateCredentials() throws Exception {
        user = registerTestUserWithDemoCredentialsAndData();

        List<Credentials> cl1 = serviceFactory.getCredentialsService().list(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, user));
        Assert.assertThat(cl1.size(), is(not(0)));

        Credentials c1 = cl1.get(0);
        c1.setPassword("newPassword");
        Credentials updatedCredentials = serviceFactory.getCredentialsService().update(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, user), c1.getId(), c1);
        Assert.assertEquals(null, updatedCredentials.getPassword());
    }

    @Test
    public void danskebankPasswordFormatTest() {
        Assert.assertFalse(isValidPassword("danskebank", "qwer"));
        Assert.assertFalse(isValidPassword("danskebank", "q123"));
        Assert.assertFalse(isValidPassword("danskebank", "42123"));
        Assert.assertFalse(isValidPassword("danskebank", "421"));

        Assert.assertTrue(isValidPassword("danskebank", "7625"));
    }

    @Test
    public void lansforsakringarPasswordFormatTest() {
        Assert.assertFalse(isValidPassword("lansforsakringar", "qwer"));
        Assert.assertFalse(isValidPassword("lansforsakringar", "q123"));
        Assert.assertFalse(isValidPassword("lansforsakringar", "42123"));
        Assert.assertFalse(isValidPassword("lansforsakringar", "421"));

        Assert.assertTrue(isValidPassword("lansforsakringar", "7625"));
    }

    @Test
    public void handelsbankenPasswordFormatTest() {
        Assert.assertFalse(isValidPassword("handelsbanken", "qwer"));
        Assert.assertFalse(isValidPassword("handelsbanken", "q123"));
        Assert.assertFalse(isValidPassword("handelsbanken", "42123"));
        Assert.assertFalse(isValidPassword("handelsbanken", "421"));

        Assert.assertTrue(isValidPassword("handelsbanken", "7625"));
    }

    @Test
    public void passwordFormatFromFileTest() throws IOException {
        File file = new File("data/test/passwords.txt");
        final Splitter splitter = Splitter.on("\t");

        List<String> passwords = Files.readLines(file, Charsets.UTF_8);

        Collections.sort(passwords, (left, right) -> {
            Iterable<String> leftArray = splitter.split(left);
            Iterable<String> rightArray = splitter.split(right);
            return ComparisonChain.start().compare(Iterables.get(leftArray, 0), Iterables.get(rightArray, 0))
                    .compare(Iterables.get(leftArray, 1), Iterables.get(rightArray, 1)).result();
        });

        for (String passwordInfo : passwords) {
            Iterable<String> credInfo = splitter.split(passwordInfo);
            String providername = Iterables.get(credInfo, 0);
            String status = Iterables.get(credInfo, 1);
            String password = Iterables.get(credInfo, 2);
            String credentialsId = Iterables.get(credInfo, 3);

            if (!isValidPassword(providername, password)) {
                System.out.println(providername + "\t" + status + "\t" + password + "\t" + credentialsId);
            }
        }

    }

    private boolean isValidPassword(String providerName, String password) {

        Provider provider = serviceContext.getRepository(ProviderRepository.class).findByName(providerName);
        List<Field> fields = provider.getFields();

        String passwordPattern = null;

        for (Field field : fields) {
            if (field.getName().equals("password")) {
                passwordPattern = field.getPattern();
            }
        }
        if (passwordPattern == null) {
            return true;
        }

        Pattern pattern = Pattern.compile(passwordPattern);

        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
