package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BPostBankTransactionDTO {

    String bookingDateTime;
    String categoryId;
    BigDecimal transactionAmount;
    String transactionCurrency;
    String counterpartyAccount;
    String counterpartyName;
    String identifier;
}
