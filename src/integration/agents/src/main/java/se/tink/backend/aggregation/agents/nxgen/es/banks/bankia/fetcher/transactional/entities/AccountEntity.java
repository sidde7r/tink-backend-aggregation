package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants.Logging;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc.AccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc.AccountDetailsResponse;
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
@Slf4j
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
    public Function<BankiaApiClient, Optional<TransactionalAccount>> toTinkAccount() {
        return apiClient -> {
            logAccountDataIfUnknownType();
            final String iban = contract.getIdentifierProductContract();
            return TransactionalAccount.nxBuilder()
                    .withTypeAndFlagsFrom(
                            BankiaConstants.PSD2_TYPE_MAPPER, contract.getProductCode())
                    .withBalance(BalanceModule.of(availableBalance.parseToExactCurrencyAmount()))
                    .withId(
                            IdModule.builder()
                                    .withUniqueIdentifier(iban.toLowerCase())
                                    .withAccountNumber(
                                            new DisplayAccountIdentifierFormatter()
                                                    .apply(
                                                            AccountIdentifier.create(
                                                                    Type.IBAN, iban)))
                                    .withAccountName(getAccountName(iban))
                                    .addIdentifier(new IbanIdentifier(iban))
                                    .build())
                    .addHolders(
                            apiClient
                                    .getAccountDetails(new AccountDetailsRequest(iban))
                                    .map(AccountDetailsResponse::getHolders)
                                    .getOrElse(Collections.emptyList()))
                    .setApiIdentifier(Iban.BANK_IDENTIFIER.extract(iban))
                    .putInTemporaryStorage(
                            BankiaConstants.StorageKey.COUNTRY, Iban.COUNTRY.extract(iban))
                    .putInTemporaryStorage(
                            BankiaConstants.StorageKey.CONTROL_DIGITS,
                            Iban.CONTROL_DIGITS.extract(iban))
                    .build();
        };
    }

    private enum Iban {
        COUNTRY(iban -> iban.substring(0, 2)),
        CONTROL_DIGITS(iban -> iban.substring(2, 4)),
        BANK_IDENTIFIER(iban -> iban.substring(4));

        private final Function<String, String> function;

        Iban(UnaryOperator<String> function) {
            this.function = function;
        }

        private String extract(String iban) {
            return this.function.apply(iban);
        }
    }
}
