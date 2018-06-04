package se.tink.backend.common.utils;

import java.util.Optional;
import com.google.common.collect.Lists;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Field;
import se.tink.backend.core.Provider;

public class CredentialsUtilsTest {

    private static Credentials createCredential(String providerName, String username, String petname, String password) {
        Credentials c = new Credentials();
        c.setProviderName(providerName);
        if (username != null) {
            c.setField("username", username);
        }
        if (password != null) {
            c.setField("password", password);
        }
        if (petname != null) {
            c.setField("petname", petname);
        }
        return c;
    }

    private Provider multiProvider;
    private Provider singleProvider;

    @Before
    public void setUp() {
        Field userNameField = new Field();
        userNameField.setName("username");
        userNameField.setMasked(false);

        Field petnameField = new Field();
        petnameField.setName("petname");
        petnameField.setMasked(false);

        Field passwordField = new Field();
        passwordField.setName("password");
        passwordField.setMasked(true);

        multiProvider = new Provider();
        multiProvider.setName("multi-field-provider");
        multiProvider.setFields(Lists.newArrayList(userNameField, petnameField, passwordField));

        singleProvider = new Provider();
        singleProvider.setName("single-field-provider");
        singleProvider.setFields(Lists.newArrayList(userNameField, passwordField));
    }

    @Test
    public void testMaskedFieldsAreIgnored() {
        Iterable<Credentials> preexistingList = Collections.singletonList(createCredential(singleProvider.getName(),
                "jens", null,
                "mysecretpassword"));
        Assert.assertTrue(CredentialsUtils.isSameAsExistingCredentials(singleProvider, Optional.empty(),
                createCredential(singleProvider.getName(), "jens", null, "myothersecretpassword"), preexistingList));
    }

    @Test
    public void testMultiFieldEqual() {
        Iterable<Credentials> preexistingList = Collections.singletonList(createCredential(singleProvider.getName(),
                "jens", "doggy",
                null));
        Assert.assertTrue(CredentialsUtils.isSameAsExistingCredentials(singleProvider, Optional.empty(),
                createCredential(singleProvider.getName(), "jens", "snaily", null), preexistingList));
    }

    @Test
    public void testMultiFieldNonEqual() {
        Iterable<Credentials> preexistingList = Collections.singletonList(createCredential(singleProvider.getName(),
                "jens", "doggy",
                null));
        Assert.assertTrue(CredentialsUtils.isSameAsExistingCredentials(singleProvider, Optional.empty(),
                createCredential(singleProvider.getName(), "jens", "doggy", null), preexistingList));
    }

    @Test
    public void testOneFieldEqual() {
        Iterable<Credentials> preexistingList = Collections.singletonList(createCredential(singleProvider.getName(),
                "jens", null,
                null));
        Assert.assertTrue(CredentialsUtils.isSameAsExistingCredentials(singleProvider, Optional.empty(),
                createCredential(singleProvider.getName(), "jens", null, null), preexistingList));
    }

    @Test
    public void testOneFieldNonEqual() {
        Iterable<Credentials> preexistingList = Collections.singletonList(createCredential(singleProvider.getName(),
                "jens", null,
                null));
        Assert.assertFalse(CredentialsUtils.isSameAsExistingCredentials(singleProvider, Optional.empty(),
                createCredential(singleProvider.getName(), "arnold", null, null), preexistingList));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongProviderForComparisonCredentials() {
        String differentProviderName = "another-providername";
        Assert.assertNotEquals(differentProviderName, multiProvider.getName());

        Iterable<Credentials> credList = Collections.singletonList(createCredential(differentProviderName, "jens",
                null,
                null));
        CredentialsUtils.isSameAsExistingCredentials(multiProvider, Optional.empty(),
                createCredential(multiProvider.getName(), "jens", "The Moose", null), credList);
    }

    @Test
    public void testSameCredentialsWhenComparingBankIdProviderAndProvier() {
        String differentProviderName = "multi-field-provider-bankid";
        Assert.assertNotEquals(differentProviderName, multiProvider.getName());

        Iterable<Credentials> credList = Collections.singletonList(createCredential(differentProviderName, "jens",
                null,
                null));
        Assert.assertTrue(CredentialsUtils.isSameAsExistingCredentials(multiProvider, Optional.empty(),
                createCredential(multiProvider.getName(), "jens", "The Moose", null), credList));
    }

    @Test
    public void testNotSameCredentialsWhenComparingBankIdProviderAndProvierDifferentValues() {
        String differentProviderName = "multi-field-provider-bankid";
        Assert.assertNotEquals(differentProviderName, multiProvider.getName());

        Iterable<Credentials> credList = Collections.singletonList(createCredential(differentProviderName, "andreas",
                null,
                null));
        Assert.assertFalse(CredentialsUtils.isSameAsExistingCredentials(multiProvider, Optional.empty(),
                createCredential(multiProvider.getName(), "jens", "The Moose", null), credList));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongProviderForCreateCredentials() {
        String differentProviderName = "another-providername";
        Assert.assertNotEquals(differentProviderName, multiProvider.getName());

        Iterable<Credentials> credList = Collections.singletonList(createCredential(multiProvider.getName(), "jens",
                null,
                null));
        CredentialsUtils.isSameAsExistingCredentials(multiProvider, Optional.empty(),
                createCredential(differentProviderName, "jens", "The Moose", null), credList);
    }

    @Test
    public void testKeepCredentialsAliveSuccess() {
        Credentials credentials = new Credentials();
        credentials.setUpdated(DateTime.now().minusMinutes(20).toDate());
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setStatus(CredentialsStatus.UPDATED);
        credentials.setProviderName("nordea-bankid");

        Assert.assertTrue(credentials.isPossibleToKeepAlive());
    }

    @Test
    public void testKeepCredentialsAliveOld() {
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setStatus(CredentialsStatus.UPDATED);
        credentials.setProviderName("nordea-bankid");

        // This is just updated and no reason to keep alive
        credentials.setUpdated(DateTime.now().minusMinutes(1).toDate());

        Assert.assertFalse(credentials.isPossibleToKeepAlive());
    }

    @Test
    public void testKeepCredentialsAliveFresh() {
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setStatus(CredentialsStatus.UPDATED);
        credentials.setProviderName("nordea-bankid");

        // This is old and no reason to keep alive
        credentials.setUpdated(DateTime.now().plusHours(3).toDate());

        Assert.assertFalse(credentials.isPossibleToKeepAlive());
    }

}
