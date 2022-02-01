package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.creditcard;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.refresh.CreditCardAccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.apiclient.BredBanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.base.BredBanquePopulaireBaseAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.common.entity.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity.AccountId;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity.OtherEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.identifiers.OtherIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BredBanquePopulaireCreditCardFetcher
        extends BredBanquePopulaireBaseAccountFetcher<CreditCardAccount> {

    public BredBanquePopulaireCreditCardFetcher(BredBanquePopulaireApiClient apiClient) {
        super(apiClient);
    }

    @Override
    protected boolean filterAccountType(AccountEntity accountEntity) {
        return accountEntity.isCreditCardAccount();
    }

    @Override
    protected Optional<CreditCardAccount> mapToAccount(AccountEntity accountEntity) {
        return Optional.of(
                CreditCardAccount.nxBuilder()
                        .withCardDetails(getCreditCardModule(accountEntity))
                        .withInferredAccountFlags()
                        .withId(getIdModule(accountEntity))
                        .setApiIdentifier(accountEntity.getResourceId())
                        .build());
    }

    private CreditCardModule getCreditCardModule(AccountEntity accountEntity) {
        List<BalanceEntity> balances =
                accountEntity.containsBalances()
                        ? accountEntity.getBalances()
                        : fetchBalances(accountEntity.getResourceId());

        return CreditCardModule.builder()
                .withCardNumber(getAccountIdentification(accountEntity))
                .withBalance(getBalance(balances))
                .withAvailableCredit(ExactCurrencyAmount.zero("EUR"))
                .withCardAlias(accountEntity.getProduct())
                .build();
    }

    private IdModule getIdModule(AccountEntity accountEntity) {
        return IdModule.builder()
                .withUniqueIdentifier(accountEntity.getResourceId())
                .withAccountNumber(accountEntity.getLinkedAccount())
                .withAccountName(accountEntity.getName())
                .addIdentifier(new OtherIdentifier(getAccountIdentification(accountEntity)))
                .build();
    }

    private List<BalanceEntity> fetchBalances(String accountResourceId) {
        return Optional.ofNullable(apiClient.fetchBalances(accountResourceId))
                .map(BalancesResponse::getBalances)
                .orElseThrow(
                        () ->
                                new CreditCardAccountRefreshException(
                                        "Cannot determine booked balance from empty list of balances."));
    }

    private ExactCurrencyAmount getBalance(List<BalanceEntity> balances) {
        return balances.stream()
                .findAny()
                .map(BalanceEntity::getBalanceAmount)
                .map(AmountEntity::toTinkAmount)
                .orElse(ExactCurrencyAmount.zero("EUR"));
    }

    private String getAccountIdentification(AccountEntity accountEntity) {
        return Optional.ofNullable(accountEntity.getAccountId())
                .map(AccountId::getOther)
                .map(OtherEntity::getIdentification)
                .orElseThrow(
                        () ->
                                new CreditCardAccountRefreshException(
                                        "AccountIdentification not found"));
    }
}
