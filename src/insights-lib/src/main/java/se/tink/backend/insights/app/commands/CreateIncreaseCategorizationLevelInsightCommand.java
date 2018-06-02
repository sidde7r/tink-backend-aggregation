package se.tink.backend.insights.app.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Objects;
import se.tink.backend.insights.core.valueobjects.UserId;

public class CreateIncreaseCategorizationLevelInsightCommand {

    private UserId userId;
    private int numberOfNonCategorizedTransactions;
    private int levelOfCategorization;

    public CreateIncreaseCategorizationLevelInsightCommand(UserId userId, int numberOfNonCategorizedTransactions,
            int levelOfCategorization) {
        validate(userId, numberOfNonCategorizedTransactions, levelOfCategorization);
        this.userId = userId;
        this.numberOfNonCategorizedTransactions = numberOfNonCategorizedTransactions;
        this.levelOfCategorization = levelOfCategorization;

    }

    public UserId getUserId() {
        return userId;
    }

    public int getNumberOfNonCategorizedTransactions() {
        return numberOfNonCategorizedTransactions;
    }

    public int getLevelOfCategorization() {
        return levelOfCategorization;
    }

    private void validate(UserId userId, int numberOfNonCategorizedTransactions, int levelOfCategorization) {
        Preconditions.checkArgument(!Objects.isNull(userId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId.value()));
        Preconditions.checkArgument(numberOfNonCategorizedTransactions > 0);
        Preconditions.checkArgument(levelOfCategorization < 100 && levelOfCategorization >= 0);
    }
}
