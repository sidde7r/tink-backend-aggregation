package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.agents.banks.seb.SebAccountIdentifierFormatter;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalAccount implements GeneralAccountEntity {
    private static final SebAccountIdentifierFormatter FORMATTER =
            new SebAccountIdentifierFormatter();

    private Optional<? extends AccountIdentifier> parsedIdentifier;

    @JsonProperty("ROW_ID")
    public String RowId;

    @JsonProperty("KORT_NAMN")
    public String Name;

    @JsonProperty("MOTT_BG_NR")
    public String BankgiroNumber;

    @JsonProperty("MOTT_NAMN_BG")
    public String BankgiroName;

    @JsonProperty("MOTT_PG_NR")
    public String PostgiroNumber;

    @JsonProperty("MOTT_NAMN_PG")
    public String PostgiroName;

    @JsonProperty("MOTT_KONTO_NR")
    public String DestinationNumber;

    @JsonProperty("BANK_PREFIX")
    public String BankPrefix;

    @JsonProperty("BANK_NAMN")
    public String BankName;

    @JsonProperty("TIMESTAMP")
    public String Timestamp;

    public boolean isPostGiro() {
        return !Strings.isNullOrEmpty(PostgiroNumber);
    }

    public boolean isBankGiro() {
        return !Strings.isNullOrEmpty(BankgiroNumber);
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        Optional<? extends AccountIdentifier> parsedIdentifier = getParsedIdentifier();

        if (!parsedIdentifier.isPresent()) {
            return new SwedishIdentifier(
                    null); // Need to return identifier, but it should not be valid
        }

        return parsedIdentifier.get();
    }

    @Override
    public String generalGetBank() {
        Optional<? extends AccountIdentifier> parsedIdentifier = getParsedIdentifier();

        if (parsedIdentifier.isPresent() && parsedIdentifier.get().is(AccountIdentifierType.SE)) {
            return parsedIdentifier.get().to(SwedishIdentifier.class).getBankName();
        } else {
            return null;
        }
    }

    @Override
    public String generalGetName() {
        return Name;
    }

    private Optional<? extends AccountIdentifier> getParsedIdentifier() {
        if (parsedIdentifier == null) {
            parsedIdentifier = FORMATTER.parseExternalIdentifier(this);
        }

        return parsedIdentifier;
    }
}
