package se.tink.backend.categorization.interfaces;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import java.util.Objects;
import java.util.Optional;
import se.tink.backend.categorization.CategorizationVector;
import se.tink.backend.core.CategorizationCommand;
import se.tink.backend.core.Transaction;

public interface Classifier {
    /**
     * Categorize a transaction and yield a probability score.
     *
     * @param transaction
     * @return an empty value if categorization failed or classifier has no clue :), otherwise a non-empty
     * {@link CategorizationVector}.
     */
    Optional<Outcome> categorize(Transaction transaction);

    class Outcome {
        public final CategorizationVector vector;
        public final CategorizationCommand command;

        public Outcome(CategorizationCommand command, CategorizationVector vector) {
            this.vector = Preconditions.checkNotNull(vector);
            this.command = Preconditions.checkNotNull(command);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Outcome outcome = (Outcome) o;
            return Objects.equals(vector, outcome.vector)
                    && command == outcome.command;
        }

        @Override
        public int hashCode() {
            return Objects.hash(vector, command);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("vector", vector)
                    .add("command", command)
                    .toString();
        }
    }
}
