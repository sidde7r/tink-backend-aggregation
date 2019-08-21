package se.tink.backend.aggregation.utils.eact;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses SEPA unstructured remittance information according to EACT standard.
 * https://eact.eu/Core/Documents/Wordpress_Old/docs/EACT_Standard_for_Remittance_Info.pdf
 */
public class UnstructuredRemittanceInformation {
    private static Pattern ELEMENT_PATTERN =
            Pattern.compile(
                    "\\G/(CNR|DOC|CINV|CREN|DEBN|RFS|RFB|PUR|URI|URL|TXT)/(.+?)(?=\\z|/(?:CNR|DOC|CINV|CREN|DEBN|RFS|RFB|PUR|URI|URL|TXT)/)");
    private static Pattern FULL_PATTERN =
            Pattern.compile("\\A(" + ELEMENT_PATTERN.pattern() + ")+\\z");

    private enum ElementType {
        CUSTOMER_NUMBER("CNR", false),
        DOCUMENT_REFERENCE("DOC", true),
        COMMERCIAL_INVOICE("CINV", true),
        CREDIT_NOTE("CREN", true),
        DEBIT_NOTE("DEBN", true),
        CHECKED_REFERENCE("RFS", true),
        UNCHECKED_REFERENCE("RFB", true),
        PURPOSE("PUR", false),
        URI("URI", false),
        URL("URL", false),
        TEXT("TXT", false);

        private final String name;
        private final boolean compound;

        ElementType(String name, boolean compound) {
            this.name = name;
            this.compound = compound;
        }

        public static ElementType fromString(String name) {
            return Arrays.stream(ElementType.values())
                    .filter(e -> e.name.equals(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Invalid element code: " + name));
        }
    }

    private ListMultimap<String, Object> fields;

    public static boolean matches(String remittanceInformation) {
        return FULL_PATTERN.matcher(remittanceInformation).matches();
    }

    private UnstructuredRemittanceInformation(ListMultimap<String, Object> fields) {
        this.fields = fields;
    }

    public static UnstructuredRemittanceInformation parse(String remittanceInformation)
            throws ParseException {
        final Matcher matcher = ELEMENT_PATTERN.matcher(remittanceInformation);
        final ListMultimap<String, Object> fields = ArrayListMultimap.create();

        while (!matcher.hitEnd()) {
            if (matcher.find()) {
                final ElementType elementType = ElementType.fromString(matcher.group(1));
                if (elementType.compound) {
                    fields.put(matcher.group(1), CompoundElement.parse(matcher.group(2)));
                } else {
                    fields.put(matcher.group(1), matcher.group(2));
                }
            } else {
                throw new ParseException(
                        "Could not parse EACT remittance information.", matcher.regionStart());
            }
        }
        return new UnstructuredRemittanceInformation(fields);
    }

    private int countElement(ElementType elementType) {
        if (fields.containsKey(elementType.name)) {
            return fields.get(elementType.name).size();
        } else {
            return 0;
        }
    }

    private String getStringElement(ElementType elementType, int index) {
        return (String) fields.get(elementType.name).get(index);
    }

    private CompoundElement getCompoundElement(ElementType elementType, int index) {
        return (CompoundElement) fields.get(elementType.name).get(index);
    }

    private BigInteger getBigIntegerElement(ElementType elementType, int index) {
        return new BigInteger(getStringElement(elementType, index));
    }

    private Optional<String> getOptionalStringElement(ElementType elementType) {
        if (countElement(elementType) == 0) {
            return Optional.empty();
        } else {
            return Optional.of(getStringElement(elementType, 0));
        }
    }

    public Optional<String> getCustomerNumber() {
        return getOptionalStringElement(ElementType.CUSTOMER_NUMBER);
    }

    public int getNumberOfDocumentReferences() {
        return countElement(ElementType.DOCUMENT_REFERENCE);
    }

    public CompoundElement getDocumentReference(int index) {
        return getCompoundElement(ElementType.DOCUMENT_REFERENCE, index);
    }

    public int getNumberOfCommercialInvoices() {
        return countElement(ElementType.COMMERCIAL_INVOICE);
    }

    public CompoundElement getCommercialInvoice(int index) {
        return getCompoundElement(ElementType.COMMERCIAL_INVOICE, index);
    }

    public int getNumberOfCreditNotes() {
        return countElement(ElementType.CREDIT_NOTE);
    }

    public CompoundElement getCreditNote(int index) {
        return getCompoundElement(ElementType.CREDIT_NOTE, index);
    }

    public int getNumberOfDebitNote() {
        return countElement(ElementType.DEBIT_NOTE);
    }

    public CompoundElement getDebitNote(int index) {
        return getCompoundElement(ElementType.DEBIT_NOTE, index);
    }

    public int getNumberOfCheckedReferences() {
        return countElement(ElementType.CHECKED_REFERENCE);
    }

    public CompoundElement getCheckedReference(int index) {
        return getCompoundElement(ElementType.CHECKED_REFERENCE, index);
    }

    public int getNumberOfUncheckedReferences() {
        return countElement(ElementType.UNCHECKED_REFERENCE);
    }

    public CompoundElement getUncheckedReference(int index) {
        return getCompoundElement(ElementType.UNCHECKED_REFERENCE, index);
    }

    public Optional<String> getPurpose() {
        return getOptionalStringElement(ElementType.PURPOSE);
    }

    public Optional<String> getUri() {
        return getOptionalStringElement(ElementType.URI);
    }

    public Optional<String> getUrl() {
        return getOptionalStringElement(ElementType.URL);
    }

    public Optional<String> getFreeText() {
        return getOptionalStringElement(ElementType.TEXT);
    }
}
