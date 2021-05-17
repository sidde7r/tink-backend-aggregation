package se.tink.backend.aggregation.agents.legacy.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.libraries.transfer.enums.TransferType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VODB {
    @JsonProperty("DBZV160")
    private String[] dbzv160;

    @JsonProperty("DEVID01")
    private DEVID01 devid01 = new DEVID01();

    @JsonProperty("HWINFO01")
    private HWINFO01 hwinfo01;

    @JsonProperty("CBEW501")
    private String[] cbew501;

    @JsonProperty("DBZV170")
    private String[] dbzv170;

    @JsonProperty("CBEW502")
    private String[] cbew502;

    // User info returned after activation, also sent as null now and then
    @JsonProperty("USRINF01")
    @Getter
    private USRINF01 usrinf01;

    // Query for a single accounts transactions
    @JsonProperty("PCBW4341")
    @Setter
    private PCBW4341 singleAccountsTransactionsQuery;

    // Query for a single accounts protected transactions
    @JsonProperty("PCBW431Z")
    @Setter
    private PCBW431Z singleAccountsProtectedTransactionsQuery;

    // This is accounts
    @JsonProperty("PCBW4211")
    private List<AccountEntity> accountEntities;

    // This is probably account transactions
    @JsonProperty("PCBW4342")
    @Getter
    private List<SebTransaction> transactions;

    // This is probably pending account transactions
    @JsonProperty("PCBW4311")
    @Getter
    private List<SebTransaction> pendingTransactions;

    // This is mortgage information
    @JsonProperty("PCBW2581")
    @Getter
    private List<PCBW2581> mortgages;

    // This is blanco loan information
    @JsonProperty("PCBW2582")
    @Getter
    private List<PCBW2582> blancoLoans;

    // Credit card

    // This is probably credit card accounts
    @JsonProperty("PCBW3201")
    @Getter
    private List<SebCreditCardAccount> creditCardAccounts;

    // Credit card transactions (not yet billed)
    @JsonProperty("PCBW3241")
    @Getter
    private List<SebCreditCardTransaction> creditCardTransactionsNotBilled;

    // Credit cards
    @JsonProperty("PCBW3242")
    @Getter
    private List<SebCreditCard> creditCards;

    // Credit card transactions (billed)
    @JsonProperty("PCBW3243")
    @Getter
    private List<SebBilledCreditCardTransaction> creditCardTransactions;

    // Transfer and payment related entities

    // Transfer request for invoices
    @JsonProperty("PCBW5211")
    private SebTransferRequestEntity invoiceTransfer;

    // Transfer request between accounts
    @JsonProperty("PCBW1221")
    private SebTransferRequestEntity bankTransfer;

    // External accounts
    @JsonProperty("PCBW189")
    @Getter
    private List<ExternalAccount> externalAccounts;

    // External transfer verification
    @JsonProperty("PCBW024")
    @Getter
    private SebTransferVerification transferVerification;

    // Response with payments (einvoices and bills)
    @JsonProperty("PCBW1241")
    private List<InvoiceTransferListEntity> invoiceTransfers;

    // Response with bank transfers
    @JsonProperty("PCBW1242")
    private List<BankTransferListEntity> bankTransfers;

    @JsonProperty("PCBW3041")
    @Getter
    private List<GiroEntity> findBGResult;

    @JsonProperty("PCBW096")
    @Getter
    private List<GiroEntity> findPGResult;

    @JsonProperty("PCBW1361")
    @Getter
    private List<UpcomingTransactionEntity> upcomingTransactions;

    @JsonProperty("PCBW203")
    private List<EInvoiceListEntity> eInvoices;

    @JsonProperty("PCBW083")
    private List<HoldingEntity> holdingEntities;

    @JsonProperty("PCBW080")
    private List<DepotEntity> depotEntities;

    @JsonProperty("PCBWF041")
    private List<InsuranceEntity> insuranceEntities;

    @JsonProperty("PCBWF061")
    private List<InsuranceAccountEntity> insuranceAccountEntities;

    @JsonProperty("PCBW173")
    private List<InsuranceHoldingEntity> insuranceHoldingEntities;

    // Not certain of the object name: IpsHoldingEntity
    @JsonProperty("PCBW174")
    private List<IpsHoldingEntity> ipsHoldingEntities;

    @JsonProperty("PCBW030")
    private List<FundAccountEntity> fundAccounts;

    @JsonProperty("PCBW8801")
    private List<PortfolioAccountMapperEntity> portfolioAccountMappers;

    @JsonProperty("RESULTO01")
    @Getter
    private RESULTO01 result;

    @JsonIgnore
    public List<TransferListEntity> getTransfers() {
        List<TransferListEntity> allTransfers = Lists.newArrayList();

        if (invoiceTransfers != null) {
            allTransfers.addAll(invoiceTransfers);
        }
        if (bankTransfers != null) {
            allTransfers.addAll(bankTransfers);
        }

        return FluentIterable.from(allTransfers).filter(Predicates.notNull()).toList();
    }

    @JsonIgnore
    public <T extends TransferListEntity> List<T> getTransfers(Class<T> ofType) {
        List<TransferListEntity> transfers = getTransfers();
        return FluentIterable.from(transfers).filter(ofType).toList();
    }

    @JsonIgnore
    public List<EInvoiceListEntity> getEInvoices() {
        if (eInvoices == null) {
            return Lists.newArrayList();
        }

        return Lists.newArrayList(
                FluentIterable.from(eInvoices).filter(Predicates.not(EInvoiceListEntity.IS_EMPTY)));
    }

    @JsonIgnore
    public void setTransfer(TransferType transferType, SebTransferRequestEntity transfer)
            throws IllegalArgumentException {
        if (Objects.equal(transferType, TransferType.BANK_TRANSFER)) {
            this.bankTransfer = transfer;
            this.invoiceTransfer = null;
        } else if (Objects.equal(transferType, TransferType.PAYMENT)) {
            this.bankTransfer = null;
            this.invoiceTransfer = transfer;
        } else {
            throw new IllegalArgumentException("Transfer type not supported");
        }
    }

    @JsonIgnore
    public List<HoldingEntity> getHoldings() {
        return holdingEntities == null ? Collections.emptyList() : holdingEntities;
    }

    @JsonIgnore
    public List<DepotEntity> getDepots() {
        return depotEntities == null ? Collections.emptyList() : depotEntities;
    }

    @JsonIgnore
    public List<InsuranceEntity> getInsuranceEntities() {
        return insuranceEntities == null ? Collections.emptyList() : insuranceEntities;
    }

    @JsonIgnore
    public List<InsuranceAccountEntity> getInsuranceAccountEntities() {
        return insuranceAccountEntities == null
                ? Collections.emptyList()
                : insuranceAccountEntities;
    }

    @JsonIgnore
    public List<InsuranceHoldingEntity> getInsuranceHoldingEntities() {
        return insuranceHoldingEntities == null
                ? Collections.emptyList()
                : insuranceHoldingEntities;
    }

    @JsonIgnore
    public List<IpsHoldingEntity> getIpsHoldingEntities() {
        return ipsHoldingEntities == null ? Collections.emptyList() : ipsHoldingEntities;
    }

    @JsonIgnore
    public List<FundAccountEntity> getFundAccounts() {
        return fundAccounts == null ? Collections.emptyList() : fundAccounts;
    }

    @JsonIgnore
    public List<AccountEntity> getAccountEntities() {
        return accountEntities == null ? Collections.emptyList() : accountEntities;
    }

    @JsonIgnore
    public List<PortfolioAccountMapperEntity> getPortfolioAccountMappers() {
        return portfolioAccountMappers == null ? Collections.emptyList() : portfolioAccountMappers;
    }
}
