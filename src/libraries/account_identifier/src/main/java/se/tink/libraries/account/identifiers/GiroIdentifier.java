package se.tink.libraries.account.identifiers;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import java.net.URI;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;
import se.tink.libraries.giro.validation.OcrValidator;

public abstract class GiroIdentifier extends AccountIdentifier {
    protected final String giroNumber;
    private String ocr;

    GiroIdentifier(String identifier) {
        this(extractGiroNumber(identifier), extractOcr(identifier));
    }

    GiroIdentifier(String giroNumber, String ocr) {
        this.giroNumber = Strings.emptyToNull(parse(giroNumber));
        this.ocr = Strings.emptyToNull(ocr);
    }

    private static String extractGiroNumber(String identifier) {
        if (!Strings.isNullOrEmpty(identifier) && identifier.contains("/")) {
            return identifier.split("/")[0];
        }

        return identifier;
    }

    private static String extractOcr(String identifier) {
        if (!Strings.isNullOrEmpty(identifier) && identifier.contains("/")) {
            return identifier.split("/")[1];
        }

        return null;
    }

    private String parse(String giroNumber) {
        if (Strings.isNullOrEmpty(giroNumber)) {
            return null;
        }

        return StringUtils.stripStart(giroNumber, "0").replace("-", "").replace(" ", "");
    }

    @Override
    public String getIdentifier() {
        if (!isValid()) {
            return null;
        }

        return hasOcr() ? String.format("%s/%s", giroNumber, ocr) : giroNumber;
    }

    public String getGiroNumber() {
        return giroNumber;
    }

    public Optional<String> getOcr() {
        return Optional.ofNullable(ocr);
    }

    private boolean hasOcr() {
        return getOcr().isPresent();
    }

    @Override
    public final boolean isGiroIdentifier() {

        return true;
    }

    @Override
    public boolean isValid() {
        // Ensure correct giro number format
        if (Strings.isNullOrEmpty(giroNumber)
                || !getGiroNumberPattern().matcher(giroNumber).matches()) {
            return false;
        }

        // Ensure any non-null ocr is a correct OCR
        if (hasOcr()) {
            OcrValidator ocrValidator =
                    new OcrValidator(OcrValidationConfiguration.hardOcrVariableLength());
            return ocrValidator.isValid(ocr);
        }

        return true;
    }

    protected abstract Pattern getGiroNumberPattern();

    @Override
    public String toUriAsString() {
        URI uri = toURI();

        if (uri == null) {
            Optional<String> ocr = getOcr();
            MoreObjects.ToStringHelper builder =
                    MoreObjects.toStringHelper(this)
                            .add("type", getType())
                            .add("identifier", getIdentifier());

            if (ocr.isPresent()) {
                builder.add("ocr", ocr);
            }

            return builder.toString();
        }

        return uri.toString();
    }
}
