package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.BasicEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.ContractEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CreditCardTransactionEntity {
    private String id;
    private String operationKey;
    private ContractEntity contract;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date transactionDate;

    private String transactionTypeIndicator;
    private BasicEntity concept;
    private AmountEntity amount;
    private AmountEntity holderAmount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date valueDate;

    private String terminalId;

    private AmountEntity maximumFeeAmount;
    // Seems like Integer, but can't be sure
    private String minimumTimeLimit;
    private String maximumTimeLimit;

    private String virtualTpvIndicator;
    private String operationTypeIndicator;
    private String customizableIndicator;
    private Double exchangeValue;
    private String cardCdepenCode;
    private PurchaserEntity purchaser;
    private AmountEntity commission;
    private ShopEntity shop;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date cardTransactionDate;

    private BasicEntity paymentMethod;
    private BasicEntity humanCategory;
    private BasicEntity humanSubcategory;

    // customPaymentInformation
    private Boolean stillCustomizable;
    private BasicEntity status;
    private Boolean isContractHolder;
    private Boolean visible;
    private String processingType;
    private BasicEntity paymentType;
    private StatementEntity statement;

    @JsonIgnore
    public CreditCardTransaction toTinkTransaction() {

        // TODO: Add info if transaction is pending as we did not see such yet
        return CreditCardTransaction.builder()
                .setAmount(amount.toTinkAmount())
                .setDate(transactionDate)
                .setDescription(shop.getName())
                .build();
    }
}
