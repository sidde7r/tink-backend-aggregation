package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount.rpc;

import java.math.BigDecimal;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SavingsResponse {

    private String accountActivatedDate;

    private Object accountName;

    private Object accountNextRenewalDate;

    private String accountNumber;

    private String accountStatus;

    private double accruedInterest;

    private BigDecimal balance;

    private ExternalPaymentEntity externalPaymentInformation;

    private double interestRate;

    private String interestRateType;

    private String name;

    private ProductInformationEntity productInformation;

    private List<TransactionEntity> transactions;

    public double getInterestRate() {
        return interestRate;
    }

    public Object getAccountNextRenewalDate() {
        return accountNextRenewalDate;
    }

    public Object getAccountName() {
        return accountName;
    }

    public double getAccruedInterest() {
        return accruedInterest;
    }

    public ProductInformationEntity getProductInformationEntity() {
        return productInformation;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public String getInterestRateType() {
        return interestRateType;
    }

    public String getAccountActivatedDate() {
        return accountActivatedDate;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public ExternalPaymentEntity getExternalPaymentEntity() {
        return externalPaymentInformation;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getName() {
        return name;
    }

    @JsonObject
    public class ExternalPaymentEntity {

        private String bankgiro;

        private String paymentReference;

        private String name;

        private String plusgiro;

        public String getBankgiro() {
            return bankgiro;
        }

        public String getPaymentReference() {
            return paymentReference;
        }

        public String getName() {
            return name;
        }

        public String getPlusgiro() {
            return plusgiro;
        }
    }

    @JsonObject
    public class ProductInformationEntity {

        private String id;

        private double effectiveInterestRate;

        private String productName;

        public String getId() {
            return id;
        }

        public double getEffectiveInterestRate() {
            return effectiveInterestRate;
        }

        public String getProductName() {
            return productName;
        }
    }
}
