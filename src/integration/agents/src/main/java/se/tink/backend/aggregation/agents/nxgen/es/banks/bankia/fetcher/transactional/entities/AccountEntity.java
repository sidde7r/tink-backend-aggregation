package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants.Logging;
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
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class AccountEntity {
    @JsonIgnore private static final Logger log = LoggerFactory.getLogger(AccountEntity.class);

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

    private String getAccountName(String iban) {
        // The bank app shows the account name as: "[ALIAS] *[LAST_FOUR_DIGITS_OF_IBAN]"
        return String.format(
                "%s *%s", contract.getAlias(), iban.substring(iban.length() - 4, iban.length()));
    }

    @JsonIgnore
    private void logAccountDataIfUnknownType() {
        if (!BankiaConstants.PSD2_TYPE_MAPPER.translate(contract.getProductCode()).isPresent()) {
            log.info(
                    "{} - {} Unknown account type: {}",
                    Logging.UNKNOWN_TRANSACTIONAL_ACCOUNT_TYPE,
                    SerializationUtils.serializeToString(this),
                    getBankiaAccountType());
        }
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {
        logAccountDataIfUnknownType();
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
