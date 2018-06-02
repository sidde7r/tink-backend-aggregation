package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchCreditCardTransactionsRequest {
    private String cardNumber;
    private String startDate;
    private String expiryDate;
    private String productCode;
    private String parallelUseCode;
    private String solidarityCode;
    private String creditAccountNumber;
    private String newestTransactionId;
    private String endDate = "";

    public FetchCreditCardTransactionsRequest setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    public FetchCreditCardTransactionsRequest setStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public FetchCreditCardTransactionsRequest setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public FetchCreditCardTransactionsRequest setProductCode(String productCode) {
        this.productCode = productCode;
        return this;
    }

    public FetchCreditCardTransactionsRequest setParallelUseCode(String parallelUseCode) {
        this.parallelUseCode = parallelUseCode;
        return this;
    }

    public FetchCreditCardTransactionsRequest setSolidarityCode(String solidarityCode) {
        this.solidarityCode = solidarityCode;
        return this;
    }

    public FetchCreditCardTransactionsRequest setCreditAccountNumber(String creditAccountNumber) {
        this.creditAccountNumber = creditAccountNumber;
        return this;
    }

    public FetchCreditCardTransactionsRequest setNewestTransactionId(String newestTransactionId) {
        this.newestTransactionId = newestTransactionId;
        return this;
    }

    public FetchCreditCardTransactionsRequest setEndDate(String endDate) {
        this.endDate = endDate;
        return this;
    }
}
