package se.tink.backend.aggregation.agents.banks.swedbank;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import java.util.Optional;
import com.google.common.base.Splitter;
import se.tink.backend.common.utils.giro.validation.GiroMessageValidator;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;
import se.tink.backend.core.transfer.Transfer;

public class SwedbankAgentUtils {
    protected static final Splitter CLEANUP_SPLITTER = Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings();
    protected static final Joiner CLEANUP_JOINER = Joiner.on(' ');

    public static String cleanDescription(String description) {
        return CLEANUP_JOINER.join(CLEANUP_SPLITTER.split(description));
    }

    public static String cleanAccountNumber(String dirtyAccountNumber) {
        return dirtyAccountNumber.replace("-", "").replace(" ", "");
    }

    public static String getReferenceTypeFor(Transfer transfer) {
        GiroMessageValidator giroValidator = GiroMessageValidator.create(OcrValidationConfiguration.softOcr());
        Optional<String> validOcr = giroValidator.validate(transfer.getDestinationMessage()).getValidOcr();

        return validOcr.isPresent() ? "OCR" : "MESSAGE";
    }
}
