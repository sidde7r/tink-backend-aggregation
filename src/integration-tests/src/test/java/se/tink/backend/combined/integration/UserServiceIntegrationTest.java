package se.tink.backend.combined.integration;

import com.sun.mail.pop3.POP3SSLStore;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.WebApplicationException;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.backend.core.Market;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserContext;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.backend.rpc.DeleteUserRequest;
import se.tink.backend.rpc.UserLoginResponse;
import se.tink.libraries.date.DateUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO this is a unit test
 */
public class UserServiceIntegrationTest extends AbstractServiceIntegrationTest {
    @Test
    public void testCreateUser() throws Exception {
        User user = registerUser(randomUsername(), "testing", createUserProfile());

        serviceFactory.getUserService().delete(authenticated(user), new DeleteUserRequest());
    }

    @Test
    public void testListMarkets() {
        List<Market> markets = serviceFactory.getUserService().listMarkets(null);

        assertTrue(markets.size() > 0);
    }

    @Test
    public void testResolutionChange() throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData();

        UserProfile p1 = serviceFactory.getUserService().getProfile(user);

        Assert.assertEquals(ResolutionTypes.MONTHLY_ADJUSTED, p1.getPeriodMode());

        p1.setPeriodMode(ResolutionTypes.MONTHLY);

        UserProfile p2 = serviceFactory.getUserService().updateProfile(user, p1);

        Assert.assertEquals(p1.getPeriodMode(), p2.getPeriodMode());

        UserContext c1 = serviceFactory.getUserService().getContext(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user));

        Assert.assertEquals(p2.getPeriodMode(), c1.getUser().getProfile().getPeriodMode());

        deleteUser(user);
    }

    @Test
    public void testCreateUserWithExistingUsername() throws Exception {
        String username = randomUsername();

        User user = registerUser(username, "testing", createUserProfile());

        boolean caughtException = false;

        try {
            registerUser(username, "testing", createUserProfile());
        } catch (Exception e) {
            caughtException = true;
        }

        assertTrue(caughtException);

        serviceFactory.getUserService().delete(authenticated(user), new DeleteUserRequest());
    }

    @Test
    public void testGetProfile() throws Exception {
        UserProfile profile1 = createUserProfile();

        User user = registerUser(randomUsername(), "testing", profile1);

        serviceFactory.getUserService().getUser(user);

        serviceFactory.getUserService().delete(authenticated(user), new DeleteUserRequest());
    }

    @Test
    @Ignore
    public void testChangeUsername() throws Exception {
        User user = registerUser(randomUsername(), "testing", createUserProfile());

        User updatedUser = new User();
        updatedUser.setUsername(randomUsername());
        updatedUser.setPassword("testing");

        User updatedUser2 = serviceFactory.getUserService().updateUser(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user), updatedUser, "testing");

        assertTrue(updatedUser2.getUsername().equals(updatedUser.getUsername()));
        assertTrue(updatedUser2.getId().equals(user.getId()));

        User updatedUser3 = serviceFactory.getUserService().getUser(updatedUser2);

        assertTrue(updatedUser3.getUsername().equals(updatedUser.getUsername()));
        assertTrue(updatedUser3.getId().equals(user.getId()));

        serviceFactory.getUserService().logout(authenticated(user), false);

        UserLoginResponse loginResponse = serviceFactory.getUserService().login(null, null, updatedUser);

        User user2 = loginResponse.getContext().getUser();

        assertTrue(user2.getId().equals(user.getId()));

        serviceFactory.getUserService().delete(authenticated(user2), new DeleteUserRequest());
    }

    @Test
    public void testChangeUsernameToAlreadyUsed() throws Exception {
        User user1a = registerUser(randomUsername(), "testing", createUserProfile());
        User user2a = registerUser(randomUsername(), "testing", createUserProfile());

        user2a.setPassword("testing");

        User updatedUser = new User();
        updatedUser.setUsername(user1a.getUsername()); // changing to same as for user1
        updatedUser.setPassword("testing");

        boolean caughtException = false;
        try {
            serviceFactory.getUserService().updateUser(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user2a), updatedUser, "testing");
        } catch (WebApplicationException e) {
            caughtException = true;
        }

        assertTrue(caughtException);

        User user2b = serviceFactory.getUserService().getUser(user2a);

        Assert.assertFalse(user2b.getUsername().equals(updatedUser.getUsername()));
        assertTrue(user2b.getId().equals(user2a.getId()));

        serviceFactory.getUserService().logout(authenticated(user2a), false);

        UserLoginResponse loginResponse = serviceFactory.getUserService().login(null, null, user2a);

        assertTrue(loginResponse.getContext().getUser().getId().equals(user2a.getId()));

        deleteUser(user2a);
        deleteUser(user1a);
    }

    @Test
    @Ignore
    public void testChangePassword() throws Exception {
        User user = registerUser(randomUsername(), "testing", createUserProfile());

        User updatedUser = new User();
        updatedUser.setUsername(user.getUsername());
        updatedUser.setPassword("testing123");

        User updatedUser2 = serviceFactory.getUserService().updateUser(authenticated(user), updatedUser, "testing");

        assertTrue(updatedUser2.getUsername().equals(updatedUser.getUsername()));
        assertTrue(updatedUser2.getId().equals(user.getId()));

        User updatedUser3 = serviceFactory.getUserService().getUser(user);

        assertTrue(updatedUser3.getUsername().equals(updatedUser.getUsername()));
        assertTrue(updatedUser3.getId().equals(user.getId()));

        serviceFactory.getUserService().logout(authenticated(user), false);

        UserLoginResponse loginResponse = serviceFactory.getUserService().login(null, null, updatedUser);

        User user2 = loginResponse.getContext().getUser();

        assertTrue(user2.getId().equals(user.getId()));

        serviceFactory.getUserService().delete(authenticated(user2), new DeleteUserRequest());
    }

    @Test
    @Ignore
    public void testForgotPasswordSending() throws Exception {
        String username = "integration-tests@tink.se";

        User user = registerUser(username, "testing", createUserProfile());

        User forgotUser = new User();
        forgotUser.setUsername(username);

        serviceFactory.getUserService().forgotPassword(forgotUser);

        DeleteUserRequest deleteUserRequest = new DeleteUserRequest();
        deleteUserRequest.setComment("test");

        serviceFactory.getUserService().delete(authenticated(user), deleteUserRequest);
    }

    protected static String fetchMailToken(String username, String password, String pattern) throws MessagingException,
            IOException {
        Properties pop3Props = new Properties();

        pop3Props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        pop3Props.setProperty("mail.pop3.socketFactory.fallback", "false");
        pop3Props.setProperty("mail.pop3.port", "995");
        pop3Props.setProperty("mail.pop3.socketFactory.port", "995");

        URLName url = new URLName("pop3", "pop.gmail.com", 995, "", username, password);

        Session session = Session.getInstance(pop3Props, null);
        POP3SSLStore store = new POP3SSLStore(session, url);

        store.connect();

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        Message[] messages = inbox.getMessages();

        String token = null;

        for (Message message : messages) {
            Address sender = message.getFrom()[0];

            if (sender.toString().contains("support@tink.se")) {
                MimeMultipart mp = (MimeMultipart) message.getContent();

                for (int i = 0; i < mp.getCount(); i++) {
                    BodyPart bp = mp.getBodyPart(i);

                    if (Pattern.compile(Pattern.quote("text/plain"), Pattern.CASE_INSENSITIVE)
                            .matcher(bp.getContentType()).find()) {
                        String content = (String) bp.getContent();

                        int tokenIndex = content.indexOf(pattern);

                        token = (content.substring(tokenIndex + pattern.length(), tokenIndex + 32 + pattern.length()));
                    }
                }
            }

            message.setFlag(Flags.Flag.DELETED, true);
        }

        inbox.close(true);
        store.close();

        return token;
    }

    @Test
    public void userProfileModificationTest() throws Exception {
        UserProfile originalProfile = createUserProfile();
        originalProfile.setPeriodAdjustedDay(23);
        originalProfile.setPeriodMode(ResolutionTypes.DAILY);

        User sessionId = registerUser(randomUsername(), "testing", originalProfile);

        UserProfile modifiableProfile = new UserProfile();
        modifiableProfile.setPeriodAdjustedDay(originalProfile.getPeriodAdjustedDay() + 1);
        modifiableProfile.setPeriodMode(ResolutionTypes.MONTHLY);

        UserProfile updatedProfile = serviceFactory.getUserService().updateProfile(sessionId, modifiableProfile);
        UserProfile profile2 = serviceFactory.getUserService().getProfile(sessionId);

        UserProfile[] profiles = new UserProfile[] { updatedProfile, profile2 };
        for (UserProfile p : profiles) {
            // modifiable
            Assert.assertEquals(modifiableProfile.getPeriodAdjustedDay(), p.getPeriodAdjustedDay());
            Assert.assertEquals(modifiableProfile.getPeriodMode(), p.getPeriodMode());
        }
        deleteUser(sessionId);
    }

    @Test
    public void returnModifiedPeriodOnChangeUserProfile() throws Exception {
        UserProfile userProfile = createUserProfile();
        userProfile.setPeriodAdjustedDay(23);
        userProfile.setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);

        User user = registerTestUserWithDemoCredentialsAndData(userProfile, "anv1");
        userProfile.setPeriodMode(ResolutionTypes.MONTHLY);

        serviceFactory.getUserService().updateProfile(user, userProfile);

        List<Period> periods = serviceFactory.getCalendarService()
                .listPeriods(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user),
                        DateUtils.toDayPeriod(DateTime.now()));

        int expSize = 1;
        assertEquals(expSize, periods.size());
        assertEquals(ResolutionTypes.MONTHLY, periods.get(0).getResolution());
    }
}
