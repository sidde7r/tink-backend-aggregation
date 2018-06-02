package se.tink.backend.insights.app.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Objects;
import se.tink.backend.insights.core.valueobjects.Address;
import se.tink.backend.insights.core.valueobjects.IdentityEventId;
import se.tink.backend.insights.core.valueobjects.UserId;

public class CreateResidenceDoYouOwnItCommand {

    private UserId userId;
    private Address address;
    private IdentityEventId identityEventId;

    public CreateResidenceDoYouOwnItCommand(UserId userId, Address address, IdentityEventId identityEventId) {
        validate(userId, address, identityEventId);
        this.userId = userId;
        this.address = address;
        this.identityEventId = identityEventId;
    }

    public UserId getUserId() {
        return userId;
    }

    public Address getAddress() {
        return address;
    }

    public IdentityEventId getIdentityEventId() {
        return identityEventId;
    }

    private void validate(UserId userId, Address address,
            IdentityEventId identityEventId) {
        Preconditions.checkArgument(!Objects.isNull(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId.value()));
        Preconditions.checkArgument(!Objects.isNull(address));
        Preconditions.checkArgument(!Objects.isNull(identityEventId));
    }
}
