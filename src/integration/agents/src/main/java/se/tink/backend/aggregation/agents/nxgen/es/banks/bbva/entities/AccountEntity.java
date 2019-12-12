package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaPredicates.IS_TRANSACTIONAL_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaTypeMappers.ACCOUNT_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Predicates;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity extends AbstractContractDetailsEntity {

    private AmountEntity currentBalance;

    private AmountEntity availableBalanceLocalCurrency;
    private AmountEntity availableBalance;
    private AmountEntity currentBalanceLocalCurrency;

    public AmountEntity getCurrentBalance() {
        return currentBalance;
    }

    public AmountEntity getAvailableBalanceLocalCurrency() {
        return availableBalanceLocalCurrency;
    }

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }

    public AmountEntity getCurrentBalanceLocalCurrency() {
        return currentBalanceLocalCurrency;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkTransactionalAccount() {
        String iban = getIban();
        String accountProductId = getAccountProductId();

        if (Strings.isNullOrEmpty(iban)) {
            return Optional.empty();
        }

        final AccountIdentifier ibanIdentifier = AccountIdentifier.create(Type.IBAN, iban);
        final DisplayAccountIdentifierFormatter formatter = new DisplayAccountIdentifierFormatter();
        final String formattedIban = ibanIdentifier.getIdentifier(formatter);

        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(ACCOUNT_TYPE_MAPPER, accountProductId)
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban.toUpperCase(Locale.ENGLISH))
                                .withAccountNumber(formattedIban)
                                .withAccountName(getProduct().getDescription())
                                .addIdentifier(ibanIdentifier)
                                .build())
                .setApiIdentifier(getId())
                .build();
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance() {
        return availableBalance.toTinkAmount();
    }

    @JsonIgnore
    private String getAccountProductId() {
        return Optional.ofNullable(getProduct())
                .map(ProductEntity::getId)
                .filter(Predicates.not(Strings::isNullOrEmpty))
                .orElse(null);
    }

    @JsonIgnore
    private String getIban() {
        return Optional.ofNullable(getFormats())
                .map(FormatsEntity::getIban)
                .map(iban -> iban.replaceAll("\\s+", ""))
                .filter(Predicates.not(Strings::isNullOrEmpty))
                .orElse(null);
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return Option.ofOptional(ACCOUNT_TYPE_MAPPER.translate(getAccountProductId()))
                .filter(IS_TRANSACTIONAL_ACCOUNT)
                .isDefined();
    }

    @JsonIgnore
    public boolean hasBalance() {
        return Objects.nonNull(availableBalance)
                && Try.of(availableBalance::toTinkAmount).isSuccess();
    }
}
