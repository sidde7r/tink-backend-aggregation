package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionAccountEntity {
    private String availableAmount;
    private String reservedAmount;
    private QuickbalanceSubscriptionEntity quickbalanceSubscription;
    private boolean currencyAccount;
    private String creditGranted;
    private boolean internalAccount;
    private String name;
    private String id;
    private String currency;
    private String balance;
    private String accountNumber;
    private String clearingNumber;
    private String fullyFormattedNumber;
    private String iban;
    private boolean availableForFavouriteAccount;
    private boolean availableForPriorityAccount;
    private String originalName;
    private ExpenseControlEntity expenseControl;
}
