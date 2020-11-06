package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.Balance;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardAccountEntity {

    private static final String INTERIM_AVAILABLE = "interimAvailable";
    private static final String FORWARD_AVAILABLE = "forwardAvailable";

    private String currency;
    private String details;
    private String maskedPan;
    private String name;
    private String product;
    private String resourceId;
    private String status;
    private String usage;

    private CardCreditLimitEntity creditLimit;
    private List<Balance> balances;
    private CardLinksEntity _links;

    @JsonIgnore
    public CreditCardAccount toCreditCardAccount() {

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(maskedPan)
                                .withBalance(getOutstandingBalance())
                                .withAvailableCredit(getAvailableCredit())
                                .withCardAlias(name)
                                .build())
                .withPaymentAccountFlag()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(maskedPan)
                                .withAccountNumber(resourceId)
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                Type.PAYMENT_CARD_NUMBER, maskedPan))
                                .build())
                .setApiIdentifier(resourceId)
                .build();
    }

    public boolean isCreditCardAccount() {
        return true;
    }

    private ExactCurrencyAmount getOutstandingBalance() {
        return getBalance(INTERIM_AVAILABLE).negate();
    }

    private ExactCurrencyAmount getAvailableCredit() {
        return getBalance(FORWARD_AVAILABLE);
    }

    private ExactCurrencyAmount getBalance(String type) {
        return balances.stream()
                .filter(x -> type.equalsIgnoreCase(x.getBalanceType()))
                .findFirst()
                .map(x -> ExactCurrencyAmount.of(x.getBalanceAmount().getAmount(), currency))
                .orElse(ExactCurrencyAmount.zero(currency));
    }
}
