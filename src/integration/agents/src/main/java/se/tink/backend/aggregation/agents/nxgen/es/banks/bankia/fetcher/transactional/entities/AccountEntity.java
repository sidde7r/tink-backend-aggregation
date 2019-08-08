package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

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

    public TransactionalAccount toTinkAccount() {
        final String iban = contract.getIdentifierProductContract();

        // These are used to fetch transactions for the account.
        final String country = iban.substring(0, 2);
        final String controlDigits = iban.substring(2, 4);
        final String bankIdentifier = iban.substring(4);

        return TransactionalAccount.builder(getTinkAccountType(), iban.toLowerCase())
                .setAccountNumber(iban)
                .setName(getAccountName(iban))
                .setBalance(availableBalance.toTinkAmount())
                .setBankIdentifier(bankIdentifier)
                .putInTemporaryStorage(BankiaConstants.StorageKey.COUNTRY, country)
                .putInTemporaryStorage(BankiaConstants.StorageKey.CONTROL_DIGITS, controlDigits)
                .build();
    }
}
