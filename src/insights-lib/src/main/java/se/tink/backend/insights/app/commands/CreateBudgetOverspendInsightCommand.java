package se.tink.backend.insights.app.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Objects;
import se.tink.backend.insights.core.valueobjects.FollowItemTransaction;
import se.tink.backend.insights.core.valueobjects.UserId;

public class CreateBudgetOverspendInsightCommand {

    private UserId userId;
    private List<FollowItemTransaction> followItemTransactions;
    private String name;

    public CreateBudgetOverspendInsightCommand(UserId userId, List<FollowItemTransaction> followItemTransaction) {
        validate(userId, followItemTransaction);
        this.userId = userId;
        this.followItemTransactions = followItemTransaction;
    }

    public UserId getUserId() {
        return userId;
    }

    public List<FollowItemTransaction> getFollowItemTransactions() {
        return followItemTransactions;
    }

    private void validate(UserId userId, List<FollowItemTransaction> followItemTransaction) {
        Preconditions.checkArgument(!Objects.isNull(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId.value()));
        Preconditions.checkArgument(followItemTransaction.size() > 0);
    }
}

