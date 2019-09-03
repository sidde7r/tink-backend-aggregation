package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;

@JsonObject
public class AccountEntity {
    @JsonProperty("contrato")
    private ContractEntity contract;

    @JsonProperty("saldoInformado")
    private boolean informedBalance;

    @JsonProperty("saldoReal")
    private AmountEntity realBalance;

    @JsonProperty("saldoDisponible")
    private AmountEntity availableBalance;

    public String getBankiaAccountType() {
        return contract.getProductCode();
    }

    public boolean isAccountTypeTransactional() {
        return BankiaConstants.ACCOUNT_TYPE_MAPPER.isOneOf(
                getBankiaAccountType(), TransactionalAccount.ALLOWED_ACCOUNT_TYPES);
    }

    private AccountTypes getTinkAccountType() {
        return BankiaConstants.ACCOUNT_TYPE_MAPPER
                .translate(getBankiaAccountType())
                .orElse(AccountTypes.OTHER);
    }

    private String getAccountName(String iban) {
        // The bank app shows the account name as: "[ALIAS] *[LAST_FOUR_DIGITS_OF_IBAN]"
        return String.format(
                "%s *%s", contract.getAlias(), iban.substring(iban.length() - 4, iban.length()));
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {
        final String iban = contract.getIdentifierProductContract();

        // These are used to fetch transactions for the account.
        final String country = iban.substring(0, 2);
        final String controlDigits = iban.substring(2, 4);
        final String bankIdentifier = iban.substring(4);

        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(BankiaConstants.PSD2_TYPE_MAPPER, contract.getProductCode())
                .withBalance(BalanceModule.of(availableBalance.parseToExactCurrencyAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban.toLowerCase())
                                .withAccountNumber(formatIban(iban))
                                .withAccountName(getAccountName(iban))
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .setApiIdentifier(bankIdentifier)
                .putInTemporaryStorage(BankiaConstants.StorageKey.COUNTRY, country)
                .putInTemporaryStorage(BankiaConstants.StorageKey.CONTROL_DIGITS, controlDigits)
                .build();
    }

    @JsonIgnore
    private String formatIban(String iban) {
        return new DisplayAccountIdentifierFormatter()
                .apply(AccountIdentifier.create(Type.IBAN, iban));
    }
}
