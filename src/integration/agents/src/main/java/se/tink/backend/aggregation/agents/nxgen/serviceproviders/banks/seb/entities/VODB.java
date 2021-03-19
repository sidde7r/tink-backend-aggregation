package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.entities.DeviceIdentification;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.entities.HardwareInformation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.entities.InitResult;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.investment.entities.InvestmentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.investment.entities.InvestmentInstrumentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.investment.entities.SimpleInsuranceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.ReservedTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.ReservedTransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.TransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.UpcomingTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
class VODB {

    @JsonProperty("DEVID01")
    public DeviceIdentification deviceIdentification;

    @JsonProperty("HWINFO01")
    public HardwareInformation hardwareInformation;

    // User info returned after activation, also sent as null now and then
    @JsonProperty("USRINF01")
    public UserInformation userInformation;

    // Company(ies) information in case the user has business engagements
    @JsonProperty("CBEW501")
    public List<BusinessEntity> businessEntities;

    @JsonProperty("RESULTO01")
    public InitResult initResult;

    @JsonProperty("PCBW4211")
    public List<AccountEntity> accountEntities;

    @JsonProperty("PCBW4341")
    public TransactionQuery transactionQuery;

    @JsonProperty("PCBW431Z")
    public ReservedTransactionQuery pendingTransactionQuery;

    @JsonProperty("PCBW4342")
    public List<TransactionEntity> transactions;

    @JsonProperty("PCBW4311")
    public List<ReservedTransactionEntity> reservedTransactions;

    @JsonProperty("PCBW4401")
    public List<AccountEntity> businessAccountEntities;

    @JsonProperty("PCBW1361")
    public List<UpcomingTransactionEntity> upcomingTransactions;

    @JsonProperty("PCBW3211")
    public List<CreditCardEntity> creditCards;

    @JsonProperty("PCBW3241")
    public List<CreditCardTransactionEntity> pendingCreditCardTransactions;

    @JsonProperty("PCBW3243")
    public List<CreditCardTransactionEntity> bookedCreditCardTransactions;

    @JsonProperty("PCBW2581")
    public List<LoanEntity> mortgageLoans;

    @JsonProperty("PCBW2582")
    public List<LoanEntity> blancoLoans;

    @JsonProperty("PCBWF041")
    public List<InvestmentEntity> investments;

    @JsonProperty("PCBWF061")
    public List<SimpleInsuranceEntity> insurances;

    @JsonProperty("PCBW173")
    public List<InvestmentInstrumentEntity> investmentInstruments;

    @JsonProperty("PCBW4421")
    public List<BusinessAccountEntity> businessAccountInfo;
}
