package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.card;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.refresh.CreditCardAccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountIdentificationOtherEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceAmountBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@NoArgsConstructor
public class LaBanquePostaleCreditCardConverter {

    public CreditCardAccount toTinkCreditCard(AccountEntity accountEntity) {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(getCreditCardModule(accountEntity))
                .withInferredAccountFlags()
                .withId(getIdModule(accountEntity))
                .setApiIdentifier(accountEntity.getResourceId())
                .build();
    }

    private CreditCardModule getCreditCardModule(AccountEntity accountEntity) {
        return CreditCardModule.builder()
                .withCardNumber(accountEntity.getAccountId().getOther().getIdentification())
                .withBalance(getBalanceCreditCard(accountEntity))
                .withAvailableCredit(ExactCurrencyAmount.of(0, "EUR"))
                .withCardAlias(accountEntity.getName())
                .build();
    }

    private ExactCurrencyAmount getBalanceCreditCard(AccountEntity accountEntity) {
        return Optional.ofNullable(accountEntity.getBalances())
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .findFirst()
                .map(BalanceBaseEntity::getBalanceAmount)
                .map(BalanceAmountBaseEntity::toAmount)
                .orElseThrow(
                        () -> new CreditCardAccountRefreshException("Balance could not be found"));
    }

    private IdModule getIdModule(AccountEntity accountEntity) {
        return IdModule.builder()
                .withUniqueIdentifier(accountEntity.getResourceId())
                .withAccountNumber(accountEntity.getLinkedAccount())
                .withAccountName(accountEntity.getName())
                .addIdentifier(getAccountIdentifier(accountEntity))
                .setProductName(accountEntity.getName())
                .build();
    }

    private AccountIdentifier getAccountIdentifier(AccountEntity accountEntity) {
        return new MaskedPanIdentifier(
                Optional.ofNullable(accountEntity.getAccountId())
                        .map(AccountIdentificationEntity::getOther)
                        .map(AccountIdentificationOtherEntity::getIdentification)
                        .orElseThrow(
                                () ->
                                        new CreditCardAccountRefreshException(
                                                "AccountIdentifier could not be build")));
    }
}
