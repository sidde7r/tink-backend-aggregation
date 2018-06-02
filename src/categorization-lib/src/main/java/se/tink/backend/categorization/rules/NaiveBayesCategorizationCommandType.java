package se.tink.backend.categorization.rules;

import com.google.common.base.MoreObjects;
import java.util.function.Predicate;
import se.tink.backend.core.CategorizationCommand;
import se.tink.backend.core.Transaction;

public enum NaiveBayesCategorizationCommandType {
    EXPENSE("expenses", t -> t.getAmount() <= 0, CategorizationCommand.GENERAL_EXPENSES),
    INCOME("income", t -> t.getAmount() > 0, CategorizationCommand.GENERAL_INCOME);

    private final String directoryString;
    private final Predicate<Transaction> predicate;
    private final CategorizationCommand command;

    NaiveBayesCategorizationCommandType(String directoryString, Predicate<Transaction> predicate,
            CategorizationCommand command) {
        this.directoryString = directoryString;
        this.predicate = predicate;
        this.command = command;
    }

    public String getDirectoryString() {
        return directoryString;
    }

    public Predicate<Transaction> getPredicate() {
        return predicate;
    }

    public CategorizationCommand getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("directoryString", directoryString)
                .toString();
    }
}
