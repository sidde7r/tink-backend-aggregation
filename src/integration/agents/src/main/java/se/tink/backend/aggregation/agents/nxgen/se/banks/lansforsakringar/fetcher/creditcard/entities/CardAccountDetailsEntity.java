package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.entities;

import com.google.common.base.MoreObjects;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardAccountDetailsEntity {
    private String accountNumber;
    private double balance;
    private String accountName;
    private String productCode;
    private String clearingNumber;
    private String ledger;
    private boolean youthAccount;
    private boolean transferFrom;
    private boolean transferTo;
    private String type;

    public String getAccountName() {
        return accountName;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("accountNumber", accountNumber)
                .add("balance", balance)
                .add("accountName", accountName)
                .add("productCode", productCode)
                .add("clearingNumber", clearingNumber)
                .add("ledger", ledger)
                .add("youthAccount", youthAccount)
                .add("transferFrom", transferFrom)
                .add("transferTo", transferTo)
                .add("type", type)
                .toString();
    }
}
