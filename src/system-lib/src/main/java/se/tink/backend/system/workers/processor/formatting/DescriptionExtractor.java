package se.tink.backend.system.workers.processor.formatting;

import se.tink.backend.core.Transaction;

public interface DescriptionExtractor {
    String getCleanDescription(Transaction transaction);
}
