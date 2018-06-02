package se.tink.backend.core;

import com.google.common.collect.ImmutableList;
import com.lambdaworks.crypto.SCryptUtil;
import java.util.Date;
import org.joda.time.DateTime;
import org.junit.Test;
import se.tink.backend.rpc.DeleteUserRequest;
import se.tink.libraries.uuid.UUIDUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class DeletedUserTest {

    @Test
    public void testCreateDeleteUserFromRequest() {
        String username = "user@tink.se";
        String userId = UUIDUtils.generateUUID();
        String hash = DeletedUser.calculateUsernameHash(username);

        User user = new User();
        user.setUsername(username);
        user.setId(userId);
        user.setCreated(DateTime.parse("2018-01-01").toDate());
        user.setProfile(new UserProfile());
        user.getProfile().setMarket("SE");

        DeleteUserRequest request = new DeleteUserRequest();
        request.setComment("this is a comment");
        request.setReasons(ImmutableList.of("reason1", "reason2"));
        request.setUserId(userId);

        DeletedUser deletedUser = DeletedUser.create(user, request);

        assertThat(deletedUser.getComment()).isEqualTo("this is a comment");
        assertThat(deletedUser.getMarket()).isEqualTo("SE");
        assertThat(deletedUser.getReasons()).containsOnly("reason1", "reason2");
        assertThat(deletedUser.getUserCreated()).isEqualTo(user.getCreated());
        assertThat(deletedUser.getUserId()).isEqualTo(userId);
        assertThat(SCryptUtil.check(username, deletedUser.getUsernameHash())).isTrue();
    }

    @Test(expected = NullPointerException.class)
    public void testNoUserShouldFail() {
        DeletedUser.create(null, new DeleteUserRequest());
    }

    @Test(expected = IllegalStateException.class)
    public void testNoUsernameShouldFail() {
        User user = new User();
        user.setUsername(null); // no username

        DeletedUser.create(user, new DeleteUserRequest());
    }


    @Test
    public void testHashNullInput() {
        assertThat(DeletedUser.calculateUsernameHash(null)).isNull();
    }

    @Test
    public void testHashEmptyInput() {
        assertThat(DeletedUser.calculateUsernameHash("")).isNull();
    }

    @Test
    public void testHashString() {
        String hash = DeletedUser.calculateUsernameHash("erik@tink.se");
        assertThat(SCryptUtil.check("erik@tink.se", hash)).isTrue();
    }

    @Test
    public void testHashWithUpperCasingString() {
        String hash = DeletedUser.calculateUsernameHash("Erik@tink.se");
        assertThat(SCryptUtil.check("erik@tink.se", hash)).isTrue();
    }

    @Test
    public void testHashWithWhitespaceString() {
        String hash = DeletedUser.calculateUsernameHash(" Erik@tink.se ");
        assertThat(SCryptUtil.check("erik@tink.se", hash)).isTrue();
    }

    @Test
    public void testPartiallyDeletedWhenStatusIsFailed() {
        DeletedUser user = new DeletedUser();
        user.setStatus(DeletedUserStatus.FAILED);
        assertThat(user.isPartiallyDeleted()).isTrue();
    }

    @Test
    public void testPartiallyDeletedWhenStatusIsInProgressAndOlderThan20Minutes() {
        DeletedUser user = new DeletedUser();
        user.setStatus(DeletedUserStatus.IN_PROGRESS);
        user.setInserted(DateTime.now().minusMinutes(21).toDate());
        assertThat(user.isPartiallyDeleted()).isTrue();
    }

    @Test
    public void testNotPartiallyDeletedWhenStatusIsCompleted() {
        DeletedUser user = new DeletedUser();
        user.setStatus(DeletedUserStatus.COMPLETED);
        assertThat(user.isPartiallyDeleted()).isFalse();
    }

    @Test
    public void testNotPartiallyDeletedWhenStatusIsCompletedAndNewerThan20Minutes() {
        DeletedUser user = new DeletedUser();
        user.setStatus(DeletedUserStatus.COMPLETED);
        user.setInserted(new Date());
        assertThat(user.isPartiallyDeleted()).isFalse();
    }
}
