package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.NonValidIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecipientAccountEntity extends AbstractResponse implements GeneralAccountEntity {

    static final Pattern PATTERN_BG_RECIPIENT = Pattern.compile(".*\\d{3,4}-\\d{4}");
    static final Pattern PATTERN_PG_RECIPIENT = Pattern.compile(".*\\d{1,7}-\\d");

    private String name;
    private String reference;
    private String additionalInfo;
    private String id;
    private int type;
    private OcrCheckEntity ocrCheck;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OcrCheckEntity getOcrCheck() {
        return ocrCheck;
    }

    public void setOcrCheck(OcrCheckEntity ocrCheck) {
        this.ocrCheck = ocrCheck;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        Matcher matcher = PATTERN_BG_RECIPIENT.matcher(reference);
        if (matcher.matches()) {
            AccountIdentifier bankGiroIdentifier = new BankGiroIdentifier(this.id);
            bankGiroIdentifier.setName(name);
            return bankGiroIdentifier;
        }

        matcher = PATTERN_PG_RECIPIENT.matcher(reference);
        if (matcher.matches()) {
            AccountIdentifier plusGiroIdentifier = new PlusGiroIdentifier(this.id);
            plusGiroIdentifier.setName(name);
            return plusGiroIdentifier;
        }

        return new NonValidIdentifier(this.id);
    }

    @Override
    public String generalGetBank() {
        return null; // Nothing
    }

    @Override
    public String generalGetName() {
        return getName();
    }
}
