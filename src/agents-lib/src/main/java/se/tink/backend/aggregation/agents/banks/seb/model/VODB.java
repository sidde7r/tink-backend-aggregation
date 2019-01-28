package se.tink.backend.aggregation.agents.banks.seb.model;

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
import se.tink.libraries.transfer.enums.TransferType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VODB {
    @JsonProperty("DBZV160")
    public String[] DBZV160;
    @JsonProperty("DEVID01")
    public DEVID01 DEVID01 = new DEVID01();
    @JsonProperty("HWINFO01")
    public HWINFO01 HWINFO01;
    @JsonProperty("CBEW501")
    public String[] CBEW501;
    @JsonProperty("DBZV170")
    public String[] DBZV170;
    @JsonProperty("CBEW502")
    public String[] CBEW502;

    //User info returned after activation, also sent as null now and then
    @JsonProperty("USRINF01")
    public USRINF01 USRINF01;

    //Query for a single accounts transactions
    @JsonProperty("PCBW4341")
    public PCBW4341 PCBW4341;
    
    //Query for a single accounts protected transactions
    @JsonProperty("PCBW431Z")
    public PCBW431Z PCBW431Z;

    // This is accounts
    @JsonProperty("PCBW4211")
    public List<AccountEntity> accountEntities;

    // This is probably account transactions
    @JsonProperty("PCBW4342")
    public List<SebTransaction> PCBW4342;

    // This is probably pending account transactions
    @JsonProperty("PCBW4311")
    public List<SebTransaction> PCBW4311;

    // This is mortgage information
    @JsonProperty("PCBW2581")
    public List<PCBW2581> PCBW2581;

    // This is blanco loan information
    @JsonProperty("PCBW2582")
    public List<PCBW2582> PCBW2582;

    // Credit card
    
    // This is probably credit card accounts
    @JsonProperty("PCBW3201")
    public List<SebCreditCardAccount> PCBW3201;
    
    // Credit card transactions (not yet billed)
    @JsonProperty("PCBW3241")
    public List<SebCreditCardTransaction> PCBW3241;
    
    // Credit cards
    @JsonProperty("PCBW3242")
    public List<SebCreditCard> PCBW3242;
    
    // Credit card transactions (billed)
    @JsonProperty("PCBW3243")
    public List<SebBilledCreditCardTransaction> PCBW3243;

    // Transfer and payment related entities

    // Transfer request for invoices
    @JsonProperty("PCBW5211")
    public SebTransferRequestEntity InvoiceTransfer;

    // Transfer request between accounts
    @JsonProperty("PCBW1221")
    public SebTransferRequestEntity BankTransfer;

    // External accounts
    @JsonProperty("PCBW189")
    public List<ExternalAccount> ExternalAccounts;

    // External transfer verification
    @JsonProperty("PCBW024")
    public SebTransferVerification TransferVerification;

    // Response with payments (einvoices and bills)
    @JsonProperty("PCBW1241")
    public List<InvoiceTransferListEntity> InvoiceTransfers;

    // Response with bank transfers
    @JsonProperty("PCBW1242")
    public List<BankTransferListEntity> BankTransfers;

    @JsonProperty("PCBW3041")
    public List<GiroEntity> FindBGResult;

    @JsonProperty("PCBW096")
    public List<GiroEntity> FindPGResult;

    @JsonProperty("PCBW1361")
    public List<UpcomingTransactionEntity> UpcomingTransactions;

    @JsonProperty("PCBW203")
    public List<EInvoiceListEntity> EInvoices;

    @JsonProperty("PCBW083")
    public List<HoldingEntity> holdingEntities;

    @JsonProperty("PCBW080")
    public List<DepotEntity> depotEntities;

    @JsonProperty("PCBWF041")
    public List<InsuranceEntity> insuranceEntities;

    @JsonProperty("PCBWF061")
    public List<InsuranceAccountEntity> insuranceAccountEntities;

    @JsonProperty("PCBW173")
    public List<InsuranceHoldingEntity> insuranceHoldingEntities;

    // Not certain of the object name: IpsHoldingEntity
    @JsonProperty("PCBW174")
    public List<IpsHoldingEntity> ipsHoldingEntities;

    @JsonProperty("PCBW030")
    public List<FundAccountEntity> fundAccounts;

    @JsonProperty("PCBW8801")
    public List<PortfolioAccountMapperEntity> portfolioAccountMappers;

    @JsonProperty("RESULTO01")
    public RESULTO01 RESULTO01;

    @JsonIgnore
    public List<TransferListEntity> getTransfers() {
        List<TransferListEntity> allTransfers = Lists.newArrayList();

        if (InvoiceTransfers != null) {
            allTransfers.addAll(InvoiceTransfers);
        }
        if (BankTransfers != null) {
            allTransfers.addAll(BankTransfers);
        }

        return FluentIterable
                .from(allTransfers)
                .filter(Predicates.notNull())
                .toList();
    }

    @JsonIgnore
    public <T extends TransferListEntity> List<T> getTransfers(Class<T> ofType) {
        List<TransferListEntity> transfers = getTransfers();
        return FluentIterable
                .from(transfers)
                .filter(ofType)
                .toList();
    }

    @JsonIgnore
    public List<EInvoiceListEntity> getEInvoices() {
        if (EInvoices == null) {
            return Lists.newArrayList();
        }

        return Lists.newArrayList(FluentIterable
                        .from(EInvoices)
                        .filter(Predicates.not(EInvoiceListEntity.IS_EMPTY)));
    }

    @JsonIgnore
    public void setTransfer(TransferType transferType, SebTransferRequestEntity transfer)
            throws IllegalArgumentException {
        if (Objects.equal(transferType, TransferType.BANK_TRANSFER)) {
            this.BankTransfer = transfer;
            this.InvoiceTransfer = null;
        } else if (Objects.equal(transferType, TransferType.PAYMENT)) {
            this.BankTransfer = null;
            this.InvoiceTransfer = transfer;
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
        return insuranceAccountEntities == null ? Collections.emptyList() : insuranceAccountEntities;
    }

    @JsonIgnore
    public List<InsuranceHoldingEntity> getInsuranceHoldingEntities() {
        return insuranceHoldingEntities == null ? Collections.emptyList() : insuranceHoldingEntities;
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
