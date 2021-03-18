package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    private String id;
    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private String currency;
    private String name;
    private String accountType;
    private String cashAccountType;
    private String bic;
    private List<BalanceEntity> balances;
    private AccountLinksEntity links;

    public String getId() {
        return id;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(ExactCurrencyAmount balance) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(balance))
                .withId(getIdModule())
                .setApiIdentifier(id)
                .build();
    }

    @JsonIgnore
    public LoanAccount toTinkLoan(ExactCurrencyAmount balance) {
        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(LoanDetails.Type.DERIVE_FROM_NAME)
                                .withBalance(balance.negate())
                                .withInterestRate(
                                        AgentParsingUtils.parsePercentageFormInterest(
                                                        balance.getExactValue().abs())
                                                .doubleValue())
                                .setInitialBalance(balance.negate())
                                .setApplicants(Collections.singletonList(name))
                                .build())
                .withId(getIdModule())
                .setApiIdentifier(id)
                .addHolderName(name)
                .build();
    }

    @JsonIgnore
    public CreditCardAccount toTinkCreditCard(
            ExactCurrencyAmount closingBooked, ExactCurrencyAmount interimAvailable) {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(maskedPan)
                                .withBalance(closingBooked)
                                .withAvailableCredit(interimAvailable)
                                .withCardAlias(name)
                                .build())
                .withoutFlags()
                .withId(getIdModule())
                .setApiIdentifier(id)
                .build();
    }

    private IdModule getIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(iban)
                .withAccountNumber(iban)
                .withAccountName(Objects.toString(name, ""))
                .addIdentifier(AccountIdentifier.create(AccountIdentifierType.IBAN, iban))
                .build();
    }
}
