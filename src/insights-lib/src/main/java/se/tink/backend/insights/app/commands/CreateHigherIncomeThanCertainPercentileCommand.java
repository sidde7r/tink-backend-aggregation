package se.tink.backend.insights.app.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Objects;
import se.tink.backend.insights.core.valueobjects.UserId;

public class CreateHigherIncomeThanCertainPercentileCommand {
    private UserId userId;
    private double percentileBetter;

    public CreateHigherIncomeThanCertainPercentileCommand(UserId userId, double percentileBetter) {
        validate(userId, percentileBetter);
        this.userId = userId;
        this.percentileBetter = percentileBetter;
    }

    public UserId getUserId() {
        return userId;
    }

    public double getPercentileBetter() {
        return percentileBetter;
    }

    private void validate(UserId userId, double percentBetter) {
        Preconditions.checkArgument(!Objects.isNull(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId.value()));
        Preconditions.checkArgument(percentBetter > 0 && percentBetter <= 100);

    }
}
