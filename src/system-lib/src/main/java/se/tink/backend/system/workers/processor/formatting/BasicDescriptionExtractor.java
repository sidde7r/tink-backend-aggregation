package se.tink.backend.system.workers.processor.formatting;

import com.google.common.base.Strings;
import se.tink.backend.core.Transaction;

public class BasicDescriptionExtractor implements DescriptionExtractor {
    private final DescriptionFormatter formatter;

    public BasicDescriptionExtractor(DescriptionFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public String getCleanDescription(Transaction transaction) {
        String description = transaction.getFormattedDescription();

        if (Strings.isNullOrEmpty(description)) {
            description = transaction.getOriginalDescription();
        }

        return formatter.clean(description);
    }
}
