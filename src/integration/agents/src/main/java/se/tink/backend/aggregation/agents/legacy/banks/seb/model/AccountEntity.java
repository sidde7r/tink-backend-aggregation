package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.banks.seb.SebAccountIdentifierFormatter;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity implements GeneralAccountEntity {
    private static final SebAccountIdentifierFormatter FORMATTER =
            new SebAccountIdentifierFormatter();

    private Optional<? extends AccountIdentifier> parsedIdentifier;

    @JsonProperty("ROW_ID")
    public String ROW_ID;

    @JsonProperty("KUND_NR_PERSORG")
    public String KUND_NR_PERSORG;

    @JsonProperty("KONTO_NR")
    public String KONTO_NR; // account number, bankId

    @JsonProperty("KTOSLAG_TXT")
    public String KTOSLAG_TXT; // type, and name if KTOBEN_TXT not present

    @JsonProperty("BOKF_SALDO")
    public String BOKF_SALDO; // balance

    @JsonProperty("DISP_BEL") // available credit
    public String DISP_BEL;

    @JsonProperty("KTOBEN_TXT")
    public String KTOBEN_TXT; // name

    @JsonProperty("KREDBEL")
    public String KREDBEL;

    @JsonProperty("KHAV")
    public String KHAV;

    @JsonProperty("BETFL")
    public String BETFL;

    @JsonProperty("INSFL")
    public String INSFL;

    @JsonProperty("UTTFL")
    public String UTTFL;

    @JsonProperty("KTOSLAG_KOD")
    public Integer KTOSLAG_KOD;

    @JsonProperty("KTOUTDR_UTSKR")
    public String KTOUTDR_UTSKR;

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
        return KTOSLAG_TXT;
    }

    private Optional<? extends AccountIdentifier> getParsedIdentifier() {
        if (parsedIdentifier == null) {
            parsedIdentifier = FORMATTER.parseInternalIdentifier(KONTO_NR);
        }

        return parsedIdentifier;
    }

    /**
     * To do payments to e.g. PG or BG types we need to have the BETFL flag == "1" to be able to do
     * these kinds of transfers (according to analyze from Charles on one of our SEB credential)
     */
    public boolean isAllowedToTransferTo(AccountIdentifier destination) {
        return isAllowedToTransferTo(destination.getType());
    }

    /**
     * To do payments to e.g. PG or BG types we need to have the BETFL flag == "1" to be able to do
     * these kinds of transfers (according to analyze from Charles on one of our SEB credential)
     */
    public boolean isAllowedToTransferTo(AccountIdentifierType destinationType) {
        if (destinationType == AccountIdentifierType.SE) {
            return true;
        } else if (destinationType == AccountIdentifierType.SE_PG
                || destinationType == AccountIdentifierType.SE_BG) {
            return Objects.equal(BETFL, "1");
        }

        return false;
    }
}
