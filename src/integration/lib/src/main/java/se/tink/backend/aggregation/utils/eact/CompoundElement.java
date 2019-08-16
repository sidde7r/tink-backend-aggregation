package se.tink.backend.aggregation.utils.eact;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class CompoundElement {
    private static String SEPARATOR = "/ ";
    private final String referenceNumber;
    private final BigDecimal amountPaid;
    private final LocalDate documentDate;

    public static CompoundElement parse(String value) throws ParseException {
        final String[] fields = value.split(SEPARATOR);
        int size = fields.length;
        if (size < 1 || size > 3) {
            throw new ParseException("Invalid number of fields for compound element.", size);
        }
        String referenceNumber = fields[0];
        BigDecimal amountPaid = null;
        LocalDate documentDate = null;
        if (size == 3) {
            documentDate = LocalDate.from(DateTimeFormatter.BASIC_ISO_DATE.parse(fields[2]));
        }
        if (size >= 2) {
            amountPaid = new BigDecimal(fields[1]);
        }
        return new CompoundElement(referenceNumber, amountPaid, documentDate);
    }

    public CompoundElement(String referenceNumber, BigDecimal amountPaid, LocalDate documentDate) {
        this.referenceNumber = referenceNumber;
        this.amountPaid = amountPaid;
        this.documentDate = documentDate;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public Optional<BigDecimal> getAmountPaid() {
        return Optional.ofNullable(amountPaid);
    }

    public Optional<LocalDate> getDocumentDate() {
        return Optional.ofNullable(documentDate);
    }
}
