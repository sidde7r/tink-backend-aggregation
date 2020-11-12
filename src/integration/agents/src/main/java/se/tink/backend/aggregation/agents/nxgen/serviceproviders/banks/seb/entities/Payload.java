package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.entities.DeviceIdentification;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.entities.HardwareInformation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.entities.InitResult;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.investment.entities.InvestmentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.investment.entities.InvestmentInstrumnentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.investment.entities.SimpleInsuranceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.ReservedTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.ReservedTransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.TransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.UpcomingTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class Payload {
    @JsonIgnore private static final Logger log = LoggerFactory.getLogger(Payload.class);

    @JsonProperty("ResultInfo")
    private ResultInfo resultInfo;

    @JsonProperty("VODB")
    private VODB vodb;

    @JsonProperty("__type")
    private String type = "SEB_CS.SEBCSService";

    @JsonProperty("ServiceInput")
    private List<ServiceInput> serviceInput;

    @JsonProperty("UserCredentials")
    private UserCredentials userCredentials;

    public Payload() {}

    @JsonIgnore
    public Payload(List<RequestComponent> components) {
        serviceInput = Lists.newArrayList();
        components.stream().forEach(this::addComponent);
    }

    @JsonIgnore
    private VODB getVodb() {
        if (Objects.isNull(vodb)) {
            vodb = new VODB();
        }
        return vodb;
    }

    @JsonIgnore
    private void addComponent(RequestComponent component) {
        if (component instanceof ServiceInput) {
            serviceInput.add((ServiceInput) component);
        } else if (component instanceof UserCredentials) {
            userCredentials = (UserCredentials) component;
        } else if (component instanceof DeviceIdentification) {
            getVodb().deviceIdentification = (DeviceIdentification) component;
        } else if (component instanceof HardwareInformation) {
            getVodb().hardwareInformation = (HardwareInformation) component;
        } else if (component instanceof TransactionQuery) {
            getVodb().transactionQuery = (TransactionQuery) component;
        } else if (component instanceof ReservedTransactionQuery) {
            getVodb().pendingTransactionQuery = (ReservedTransactionQuery) component;
        } else {
            throw new NotImplementedException(
                    "Request component not implemented: " + component.getClass().toString());
        }
    }

    @JsonIgnore
    public boolean hasErrors() {
        final List<ResultInfoMessage> messages = resultInfo.getMessages();
        return messages != null && messages.size() > 0;
    }

    @JsonIgnore
    public List<ResultInfoMessage> getMessages() {
        return resultInfo.getMessages();
    }

    @JsonIgnore
    public boolean isValid() {
        return vodb != null;
    }

    @JsonIgnore
    public InitResult getInitResult() {
        Preconditions.checkNotNull(vodb);
        return vodb.initResult;
    }

    @JsonIgnore
    public UserInformation getUserInformation() {
        Preconditions.checkNotNull(vodb);
        return vodb.userInformation;
    }

    @JsonIgnore
    public List<BusinessEntity> getBusinessEntities() {
        Preconditions.checkNotNull(vodb);
        return vodb.businessEntities;
    }

    @JsonIgnore
    public List<AccountEntity> getAccountEntities() {
        Preconditions.checkNotNull(vodb);
        return vodb.accountEntities;
    }

    @JsonIgnore
    public List<AccountEntity> getBusinessAccountEntities() {
        Preconditions.checkNotNull(vodb);
        return vodb.businessAccountEntities;
    }

    @JsonIgnore
    public TransactionQuery getTransactionQuery() {
        Preconditions.checkNotNull(vodb);
        return vodb.transactionQuery;
    }

    @JsonIgnore
    public List<TransactionEntity> getTransactions() {
        Preconditions.checkNotNull(vodb);
        return vodb.transactions;
    }

    @JsonIgnore
    public List<ReservedTransactionEntity> getReservedTransactions() {
        Preconditions.checkNotNull(vodb);
        return vodb.reservedTransactions;
    }

    @JsonIgnore
    public List<UpcomingTransactionEntity> getUpcomingTransactions() {
        Preconditions.checkNotNull(vodb);
        return vodb.upcomingTransactions;
    }

    @JsonIgnore
    public List<CreditCardEntity> getCreditCards() {
        Preconditions.checkNotNull(vodb);
        return vodb.creditCards;
    }

    @JsonIgnore
    public List<CreditCardTransactionEntity> getPendingCreditCardTransactions() {
        Preconditions.checkNotNull(vodb);
        return vodb.pendingCreditCardTransactions;
    }

    @JsonIgnore
    public List<CreditCardTransactionEntity> getBookedCreditCardTransactions() {
        Preconditions.checkNotNull(vodb);
        return vodb.bookedCreditCardTransactions;
    }

    @JsonIgnore
    public List<LoanEntity> getMortgageLoans() {
        Preconditions.checkNotNull(vodb);

        return Optional.ofNullable(vodb.mortgageLoans).orElse(Collections.emptyList());
    }

    @JsonIgnore
    public List<LoanEntity> getBlancoLoans() {
        Preconditions.checkNotNull(vodb);

        return Optional.ofNullable(vodb.blancoLoans).orElse(Collections.emptyList());
    }

    @JsonIgnore
    public List<InvestmentEntity> getInvestments() {
        Preconditions.checkNotNull(vodb);
        return vodb.investments;
    }

    @JsonIgnore
    public List<SimpleInsuranceEntity> getInsurances() {
        Preconditions.checkNotNull(vodb);
        return vodb.insurances;
    }

    @JsonIgnore
    public List<InvestmentInstrumnentEntity> getInvestmentInstruments() {
        Preconditions.checkNotNull(vodb);
        return vodb.investmentInstruments;
    }

    @JsonIgnore
    public String getHolderNameBusiness() {
        Preconditions.checkNotNull(vodb);

        if (CollectionUtils.isNotEmpty(vodb.businessAccountInfo)) {
            return vodb.businessAccountInfo.get(0).getHolderName();
        }

        log.error("Business account info not present - can't get business holder name");
        // Returning null since getHolderName() will default to another value if null.
        return null;
    }
}
