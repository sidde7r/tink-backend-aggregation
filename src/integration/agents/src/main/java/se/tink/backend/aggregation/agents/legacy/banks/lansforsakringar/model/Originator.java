package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.NonValidIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Originator implements GeneralAccountEntity {

    static final Pattern PATTERN_BG_RECIPIENT = Pattern.compile("^\\d{3,4}-\\d{4}");
    static final Pattern PATTERN_PG_RECIPIENT = Pattern.compile("^\\d{1,7}-\\d");

    private String giroNumber;
    private String name;
    private String ocrType;

    public String getGiroNumber() {
        return giroNumber;
    }

    public String getName() {
        return name;
    }

    public String getOcrType() {
        return ocrType;
    }

    public void setGiroNumber(String giroNumber) {
        this.giroNumber = giroNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOcrType(String ocrType) {
        this.ocrType = ocrType;
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        Matcher matcher = PATTERN_BG_RECIPIENT.matcher(this.giroNumber);
        if (matcher.matches()) {
            AccountIdentifier bankGiroIdentifier = new BankGiroIdentifier(this.giroNumber);
            bankGiroIdentifier.setName(name);
            return bankGiroIdentifier;
        }

        matcher = PATTERN_PG_RECIPIENT.matcher(this.giroNumber);
        if (matcher.matches()) {
            AccountIdentifier plusGiroIdentifier = new PlusGiroIdentifier(this.giroNumber);
            plusGiroIdentifier.setName(name);
            return plusGiroIdentifier;
        }

        return new NonValidIdentifier(this.giroNumber);
    }

    @Override
    public String generalGetBank() {
        return null;
    }

    @Override
    public String generalGetName() {
        return this.name;
    }

}
