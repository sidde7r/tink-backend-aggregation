package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
import se.tink.libraries.account.identifiers.NorwegianIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
public class DnbAccountMapper {

    private static final String[] SAVING_ACCOUNT_PRODUCT_NAMES = {
        "SPAREKONTO", "SAVING", "SUPERSPAR", "PLASSERINGSKONTO"
    };
    private static final List<DnbBalanceType> BOOKED_BALANCE_TYPE_PRIORITIES =
            ImmutableList.of(
                    DnbBalanceType.OPENING_BOOKED,
                    DnbBalanceType.CLOSING_BOOKED,
                    DnbBalanceType.EXPECTED,
                    DnbBalanceType.INTERIM_AVAILABLE);
    private static final List<DnbBalanceType> AVAILABLE_BALANCE_TYPE_PRIORITIES =
            ImmutableList.of(
                    DnbBalanceType.INTERIM_AVAILABLE,
                    DnbBalanceType.EXPECTED,
                    DnbBalanceType.FORWARD_AVAILABLE);

    public Optional<TransactionalAccount> toTinkAccount(
            AccountEntity accountEntity, BalancesResponse balancesResponse) {
        try {
            return TransactionalAccount.nxBuilder()
                    .withType(getAccountType(accountEntity.getName()))
                    .withPaymentAccountFlag()
                    .withBalance(getBalanceModule(balancesResponse))
                    .withId(
                            IdModule.builder()
                                    .withUniqueIdentifier(accountEntity.getBban())
                                    .withAccountNumber(accountEntity.getBban())
                                    .withAccountName(accountEntity.getName())
                                    .addIdentifier(new NorwegianIdentifier(accountEntity.getBban()))
                                    .build())
                    .setApiIdentifier(accountEntity.getBban())
                    .addHolderName(accountEntity.getOwnerName())
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

    private BalanceModule getBalanceModule(BalancesResponse balancesResponse) {
        ExactCurrencyAmount bookedBalance = getBookedBalance(balancesResponse);
        Optional<ExactCurrencyAmount> availableBalance = tryGetAvailableBalance(balancesResponse);

        if (availableBalance.isPresent()) {
            return BalanceModule.builder()
                    .withBalance(bookedBalance)
                    .setAvailableBalance(availableBalance.get())
                    .build();
        }

        return BalanceModule.of(bookedBalance);
    }

    private ExactCurrencyAmount getBookedBalance(BalancesResponse balancesResponse) {
        return getBalanceAmount(balancesResponse, BOOKED_BALANCE_TYPE_PRIORITIES)
                .orElseThrow(
                        () ->
                                new AccountRefreshException(
                                        DnbConstants.ErrorMessages.WRONG_BALANCE_TYPE));
    }

    private Optional<ExactCurrencyAmount> tryGetAvailableBalance(
            BalancesResponse balancesResponse) {
        Optional<ExactCurrencyAmount> maybeAvailableBalance =
                getBalanceAmount(balancesResponse, AVAILABLE_BALANCE_TYPE_PRIORITIES);

        if (!maybeAvailableBalance.isPresent()) {
            log.info(
                    "Could not map available balance from balance types: {}",
                    balancesResponse.getBalances().stream()
                            .map(Balance::getBalanceType)
                            .collect(Collectors.toList()));
        }
        return maybeAvailableBalance;
    }

    private Optional<ExactCurrencyAmount> getBalanceAmount(
            BalancesResponse balancesResponse, List<DnbBalanceType> matchingBalanceTypes) {
        return matchingBalanceTypes.stream()
                .map(balancesResponse::getBalanceOfType)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Balance::toTinkAmount)
                .findFirst();
    }
}
