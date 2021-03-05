package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.StandardResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class SBABTransactionalAccountsEntity extends StandardResponse {
    private String accountNumber;
    private String accountType;
    private BigDecimal accruedInterestCredit;
    private BigDecimal availableForWithdrawal;
    private BigDecimal balance;
    private BigDecimal interestRate;
    private List<AccountHoldersEntity> accountHolders;
    private List<MandatesEntity> mandates;
    private String name;
    private String status;
    private TransfersEntity transfers;

    public Optional<TransactionalAccount> toTinkTransactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(SBABConstants.ACCOUNT_TYPE_MAPPER, accountType)
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(name)
                                .addIdentifier(new SwedishIdentifier(accountNumber))
                                .build())
                .addParties(getParties())
                .build();
    }

    private ExactCurrencyAmount getBalance() {
        return ExactCurrencyAmount.of(balance, SBABConstants.CURRENCY);
    }

    private List<Party> getParties() {
        return mandates.stream().map(MandatesEntity::toParty).collect(Collectors.toList());
    }
}
