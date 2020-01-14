package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.mapper;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class DefaultPartnerAccountMapper implements NordeaPartnerAccountMapper {
    @Override
    public Optional<TransactionalAccount> toTinkTransactionalAccount(AccountEntity account) {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(
                        NordeaPartnerConstants.TRANSACTIONAL_ACCOUNT_TYPE_MAPPER,
                        account.getCategory())
                .withBalance(
                        BalanceModule.of(
                                ExactCurrencyAmount.of(
                                        account.getAvailableBalance(), account.getCurrency())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(account.getIban())
                                .withAccountNumber(account.getDisplayAccountNumber())
                                .withAccountName(account.getNickname())
                                .addIdentifier(
                                        AccountIdentifier.create(Type.IBAN, account.getIban()))
                                .build())
                .addHolderName(account.getHolderName())
                .setApiIdentifier(account.getAccountId())
                .build();
    }
}
