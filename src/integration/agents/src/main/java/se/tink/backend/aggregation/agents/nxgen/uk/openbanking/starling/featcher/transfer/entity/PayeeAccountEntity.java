package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.entity;

import java.util.Optional;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;

@JsonObject
public class PayeeAccountEntity {

    private String payeeAccountUid;
    private String description;
    private boolean defaultAccount;
    private String countryCode;
    private String accountIdentifier;
    private String bankIdentifier;
    private String bankIdentifierType;

    public String getPayeeAccountUid() {
        return payeeAccountUid;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDefaultAccount() {
        return defaultAccount;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getAccountIdentifier() {
        return accountIdentifier;
    }

    public String getBankIdentifier() {
        return bankIdentifier;
    }

    public String getBankIdentifierType() {
        return bankIdentifierType;
    }

    public boolean equalsSortCodeIdentifer(SortCodeIdentifier identifier) {
        return identifier.getSortCode().equalsIgnoreCase(bankIdentifier)
                && identifier.getAccountNumber().equalsIgnoreCase(accountIdentifier);
    }

    public Optional<PayeeGeneralAccount> toGeneralAccountEntity() {

        Optional<AccountIdentifierType> type =
                StarlingConstants.ACCOUNT_IDENTIFIER_MAPPER.translate(bankIdentifierType);

        // Returns empty if identifier is not one supported by Tink
        return type.map(
                idType ->
                        new PayeeGeneralAccount(
                                AccountIdentifier.create(
                                        idType, bankIdentifier + accountIdentifier),
                                bankIdentifier,
                                description));
    }

    public class PayeeGeneralAccount implements GeneralAccountEntity {

        private final AccountIdentifier identifier;
        private final String bank;
        private final String name;

        public PayeeGeneralAccount(AccountIdentifier identifier, String bank, String name) {
            this.identifier = identifier;
            this.bank = bank;
            this.name = name;
        }

        @Override
        public AccountIdentifier generalGetAccountIdentifier() {
            return identifier;
        }

        @Override
        public String generalGetBank() {
            return bank;
        }

        @Override
        public String generalGetName() {
            return name;
        }
    }
}
