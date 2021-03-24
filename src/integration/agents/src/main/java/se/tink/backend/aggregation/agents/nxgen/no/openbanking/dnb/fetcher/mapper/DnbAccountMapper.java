package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper;

import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.Balance;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.BalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
public class DnbAccountMapper {

    private static final String[] SAVING_ACCOUNT_PRODUCT_NAMES = {
        "SPAREKONTO", "SAVING", "SUPERSPAR", "PLASSERINGSKONTO"
    };
    private static final String[] BOOKED_BALANCE_TYPE_PRIORITIES = {
        "openingBooked", "closingBooked", "expected", "authorised", "interimAvailable"
    };

    public Optional<TransactionalAccount> toTinkAccount(
            AccountEntity accountEntity, BalancesResponse balancesResponse) {
        try {
            return TransactionalAccount.nxBuilder()
                    .withType(getAccountType(accountEntity.getName()))
                    .withPaymentAccountFlag()
                    .withBalance(BalanceModule.of(getAmount(balancesResponse)))
                    .withId(
                            IdModule.builder()
                                    .withUniqueIdentifier(accountEntity.getBban())
                                    .withAccountNumber(accountEntity.getBban())
                                    .withAccountName(accountEntity.getName())
                                    .addIdentifier(
                                            AccountIdentifier.create(
                                                    AccountIdentifierType.NO,
                                                    accountEntity.getBban()))
                                    .build())
                    .setApiIdentifier(accountEntity.getBban())
                    .build();
        } catch (RuntimeException e) {
            log.error("Failed to parse account, it will be skipped.", e);
            return Optional.empty();
        }
    }

    private TransactionalAccountType getAccountType(String name) {
        boolean isSavings =
                Arrays.stream(SAVING_ACCOUNT_PRODUCT_NAMES)
                        .map(String::toLowerCase)
                        .anyMatch(x -> name.toLowerCase().contains(x));

        return isSavings ? TransactionalAccountType.SAVINGS : TransactionalAccountType.CHECKING;
    }

    private ExactCurrencyAmount getAmount(BalancesResponse balancesResponse) {
        return Arrays.stream(BOOKED_BALANCE_TYPE_PRIORITIES)
                .map(balancesResponse::getBalanceOfType)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Balance::toTinkAmount)
                .findFirst()
                .orElseThrow(
                        () ->
                                new AccountRefreshException(
                                        DnbConstants.ErrorMessages.WRONG_BALANCE_TYPE));
    }
}
