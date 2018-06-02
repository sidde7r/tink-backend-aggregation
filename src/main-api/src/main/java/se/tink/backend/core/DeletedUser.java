package se.tink.backend.core;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.lambdaworks.crypto.SCryptUtil;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import se.tink.backend.rpc.DeleteUserRequest;

@Entity
@Table(name = "users_deleted")
public class DeletedUser {
    private static final Minutes DELETED_TIMEOUT = Minutes.minutes(20);
    private static final Joiner JOINER = Joiner.on(",").skipNulls();
    private static final Splitter SPLITTER = Splitter.on(',');

    private String comment;
    private Date userCreated;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private Date inserted;
    @Column(name = "`reasons`")
    private String reasonsSerialized;
    private String userId;
    private String market;
    private String usernameHash;
    @Deprecated
    private String username;
    @Enumerated(EnumType.STRING)
    private DeletedUserStatus status;

    @Deprecated
    public String getUsername() {
        return username;
    }

    @Deprecated
    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public Date getInserted() {
        return inserted;
    }

    public List<String> getReasons() {
        if (reasonsSerialized == null) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(SPLITTER.split(reasonsSerialized));
    }

    public String getUserId() {
        return userId;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setInserted(Date inserted) {
        this.inserted = inserted;
    }

    public void setReasons(List<String> reasons) {
        this.reasonsSerialized = JOINER.join(reasons);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getUserCreated() {
        return userCreated;
    }

    public void setUserCreated(Date userCreated) {
        this.userCreated = userCreated;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsernameHash() {
        return usernameHash;
    }

    public void setUsernameHash(String usernameHash) {
        this.usernameHash = usernameHash;
    }

    public DeletedUserStatus getStatus() {
        return status;
    }

    public void setStatus(DeletedUserStatus status) {
        this.status = status;
    }

    public static DeletedUser create(User user, DeleteUserRequest request) {
        Preconditions.checkNotNull(user, "User must not be null.");
        Preconditions.checkState(!Strings.isNullOrEmpty(user.getUsername()), "Username must not be null or empty.");

        DeletedUser deletedUser = new DeletedUser();

        // Input from user when removing the account
        deletedUser.setComment(request.getComment());

        if (request.getReasons() != null) {
            deletedUser.setReasons(request.getReasons());
        }

        // Data from user object
        deletedUser.setUserId(user.getId());
        deletedUser.setMarket(user.getProfile().getMarket());
        deletedUser.setUserCreated(user.getCreated());
        deletedUser.setUsernameHash(calculateUsernameHash(user.getUsername()));

        // Default data
        deletedUser.setInserted(new Date());
        deletedUser.setStatus(DeletedUserStatus.IN_PROGRESS);

        return deletedUser;
    }

    /**
     * Calculates a hash of the provided username. Used so that we can check if a user had an account without storing
     * the actual email or username.
     */
    public static String calculateUsernameHash(String username) {
        if (Strings.isNullOrEmpty(username)) {
            return null;
        } else {
            return SCryptUtil.scrypt(username.toLowerCase().trim(), 16384, 8, 1);
        }
    }

    /**
     * A user is considered to be partially deleted if the status is `FAILED` or if the status has been `IN_PROGRESS`
     * for over 20 minutes.
     */
    public boolean isPartiallyDeleted() {
        return Objects.equals(status, DeletedUserStatus.FAILED) || (
                Objects.equals(status, DeletedUserStatus.IN_PROGRESS) && new DateTime(inserted).plus(DELETED_TIMEOUT)
                        .isBeforeNow());
    }
}
