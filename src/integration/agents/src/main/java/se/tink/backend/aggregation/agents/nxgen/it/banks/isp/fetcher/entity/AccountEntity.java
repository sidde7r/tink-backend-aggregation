package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Slf4j
@Data
public class AccountEntity {

    private static Map<String, TransactionalAccountType> transactionalAccountTypes =
            new HashMap<>();
    private static Map<String, LoanDetails.Type> loanAccountTypes = new HashMap<>();

    static {
        transactionalAccountTypes.put("XME Conto", TransactionalAccountType.CHECKING);

        loanAccountTypes.put("Conto per Merito", LoanDetails.Type.STUDENT);
    }

    @JsonProperty("coordinateDaVisualizzare")
    private AccountDetails accountDetails;

    @JsonProperty("descrizione")
    private String description;

    @JsonProperty("descrizioneCommerciale")
    private String detailedDescription;

    @JsonProperty("divisa")
    private String currency;

    private String id;

    @JsonProperty("listaIntestatariCompleta")
    private List<AccountHolderEntity> accountHolders;

    @JsonProperty("operativo")
    private boolean active;

    @JsonProperty("saldo")
    private BalanceEntity balance;

    public void logAccountDetailsIfTypeUnknown() {
        if (!transactionalAccountTypes.containsKey(detailedDescription)
                && !loanAccountTypes.containsKey(detailedDescription)) {
            log.info(
                    "Unknown account type with description [{}] and detailed description [{}]",
                    description,
                    detailedDescription);
        }
    }

    public Optional<LoanAccount> toLoanAccount() {
        if (!active) {
            return Optional.empty();
        }
        LoanDetails.Type loanType = loanAccountTypes.get(detailedDescription);
        return Optional.of(
                LoanAccount.nxBuilder()
                        .withLoanDetails(
                                LoanModule.builder()
                                        .withType(loanType)
                                        .withBalance(
                                                ExactCurrencyAmount.of(
                                                        balance.getCurrentBalance(), currency))
                                        .withInterestRate(0d) // api does not return interest rate
                                        .build())
                        .withId(getIdModule())
                        .setApiIdentifier(id)
                        .addParties(
                                accountHolders.stream()
                                        .map(a -> new Party(a.getFullName(), Party.Role.HOLDER))
                                        .toArray(Party[]::new))
                        .build());
    }

    public Optional<TransactionalAccount> toTransactionalAccount() {
        if (!active) {
            return Optional.empty();
        }
        TransactionalAccountType transactionalAccountType =
                transactionalAccountTypes.get(detailedDescription);
        return TransactionalAccount.nxBuilder()
                .withType(transactionalAccountType)
                .withInferredAccountFlags()
                .withBalance(
                        BalanceModule.builder()
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                balance.getAvailableBalance(), currency))
                                .setAvailableBalance(
                                        ExactCurrencyAmount.of(
                                                balance.getAvailableBalance(), currency))
                                .build())
                .withId(getIdModule())
                .setApiIdentifier(id)
                .addParties(
                        accountHolders.stream()
                                .map(a -> new Party(a.getFullName(), Party.Role.HOLDER))
                                .toArray(Party[]::new))
                .build();
    }

    private IdModule getIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(accountDetails.getIban())
                .withAccountNumber(accountDetails.getIban())
                .withAccountName(detailedDescription)
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifier.Type.IBAN, accountDetails.getIban()))
                .build();
    }

    public boolean isTransactionalAccount() {
        return transactionalAccountTypes.containsKey(detailedDescription);
    }

    public boolean isLoanAccount() {
        return loanAccountTypes.containsKey(detailedDescription);
    }
}
