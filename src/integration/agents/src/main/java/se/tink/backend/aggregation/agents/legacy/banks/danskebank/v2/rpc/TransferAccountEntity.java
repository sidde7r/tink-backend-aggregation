package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.DanskeBankAccountIdentifierFormatter;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferAccountEntity implements GeneralAccountEntity {
    private static final DanskeBankAccountIdentifierFormatter IDENTIFIER_FORMATTER =
            new DanskeBankAccountIdentifierFormatter();

    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("AccountNumber")
    private String accountNumber;

    @JsonProperty("AccountName")
    private String accountName;

    @JsonProperty("Bank")
    private String bank;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        // The accountNumber sometimes contains the clearing number (separated with a space), e.g.
        // "clearing accountnr".
        // However, when matching transfer destinations/sources with collected accounts they wont
        // match unless
        // we remove the clearing number (which is not present in the accounts list).
        // This regex removes the clearing number if present.
        return accountNumber.replaceFirst("^[^\\s]*\\s", "");
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    /*
     * The methods below are for general purposes
     */

    /**
     * Once this entity has been initialized we don't need to re-parse the identifier each time.
     * Just reuse the same identifier object.
     */
    private SwedishIdentifier cachedGeneralGetAccountIdentifier;

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        if (cachedGeneralGetAccountIdentifier == null) {
            cachedGeneralGetAccountIdentifier =
                    IDENTIFIER_FORMATTER.parseSwedishIdentifier(bank, getAccountNumber());
        }

        return cachedGeneralGetAccountIdentifier;
    }

    @Override
    public String generalGetBank() {
        AccountIdentifier accountIdentifier = generalGetAccountIdentifier();

        if (accountIdentifier.is(AccountIdentifier.Type.SE) && accountIdentifier.isValid()) {
            return accountIdentifier.to(SwedishIdentifier.class).getBankName();
        } else {
            return null;
        }
    }

    @Override
    public String generalGetName() {
        return getAccountName();
    }
}
