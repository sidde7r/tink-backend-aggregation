package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.entities.BusinessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.entities.Payload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.entities.ResultInfoMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.entities.SystemStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.entities.UserInformation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.investment.entities.InvestmentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.investment.entities.InvestmentInstrumentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.investment.entities.SimpleInsuranceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.ReservedTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.TransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities.UpcomingTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Response {
    @JsonProperty("d")
    public Payload payload = new Payload();

    @JsonProperty("X")
    public SystemStatus systemStatus = new SystemStatus();

    /**
     * Request is an error of the messages size are larger than 0. There can be several error
     * messages on a response.
     */
    @JsonIgnore
    public boolean hasErrors() {
        return payload != null && payload.hasErrors();
    }

    @JsonIgnore
    public List<ResultInfoMessage> getErrors() {
        if (hasErrors()) {
            return payload.getMessages();
        } else {
            return ImmutableList.of();
        }
    }

    // Some errors might be missing an error text
    @JsonIgnore
    public Optional<ResultInfoMessage> getFirstErrorWithErrorText() {
        return getErrors().stream().filter(ResultInfoMessage::hasText).findFirst();
    }

    @JsonIgnore
    public Optional<String> getFirstErrorMessage() {
        return getFirstErrorWithErrorText().map(ResultInfoMessage::getErrorText);
    }

    /** Checks that the response has a VODB object */
    @JsonIgnore
    public boolean isValid() {
        if (hasErrors()) {
            return false;
        }

        if (Objects.isNull(payload) || !payload.isValid()) {
            return false;
        }
        return true;
    }

    @JsonIgnore
    public String getInitResult() {
        Preconditions.checkState(isValid());
        return payload.getInitResult().getInitResult();
    }

    @JsonIgnore
    public SystemStatus getSystemStatus() {
        return systemStatus;
    }

    @JsonIgnore
    public UserInformation getUserInformation() {
        return payload.getUserInformation();
    }

    @JsonIgnore
    public BusinessEntity getMatchingCompanyInformation(String orgNumber) {
        return payload.getBusinessEntities().stream()
                .filter(
                        businessEntity ->
                                businessEntity
                                        .getCompanyNumber()
                                        .substring(0, 10)
                                        .equals(orgNumber))
                .findFirst()
                .orElseThrow(LoginError.INCORRECT_CREDENTIALS::exception);
    }

    @JsonIgnore
    public Optional<List<AccountEntity>> getAccountEntities() {
        return Optional.ofNullable(payload.getAccountEntities());
    }

    @JsonIgnore
    public Optional<List<AccountEntity>> getBusinessAccountEntities() {
        return Optional.ofNullable(payload.getBusinessAccountEntities());
    }

    @JsonIgnore
    public TransactionQuery getTransactionQuery() {
        return payload.getTransactionQuery();
    }

    @JsonIgnore
    public Optional<List<TransactionEntity>> getTransactions() {
        return Optional.ofNullable(payload.getTransactions());
    }

    @JsonIgnore
    public List<ReservedTransactionEntity> getReservedTransactions() {
        return payload.getReservedTransactions();
    }

    @JsonIgnore
    public Collection<UpcomingTransactionEntity> getUpcomingTransactions() {
        return payload.getUpcomingTransactions();
    }

    @JsonIgnore
    public Optional<List<CreditCardEntity>> getCreditCards() {
        return Optional.ofNullable(payload.getCreditCards());
    }

    @JsonIgnore
    public List<CreditCardTransactionEntity> getPendingCreditCardTransactions() {
        return payload.getPendingCreditCardTransactions();
    }

    @JsonIgnore
    public List<CreditCardTransactionEntity> getBookedCreditCardTransactions() {
        return payload.getBookedCreditCardTransactions();
    }

    @JsonIgnore
    public List<InvestmentEntity> getInvestments() {
        return payload.getInvestments();
    }

    @JsonIgnore
    public List<SimpleInsuranceEntity> getInsurances() {
        return payload.getInsurances();
    }

    @JsonIgnore
    public List<InvestmentInstrumentEntity> getInvestmentInstruments() {
        return payload.getInvestmentInstruments();
    }

    @JsonIgnore
    public List<LoanEntity> getMortgageLoans() {
        return payload.getMortgageLoans();
    }

    @JsonIgnore
    public List<LoanEntity> getBlancoLoans() {
        return payload.getBlancoLoans();
    }

    @JsonIgnore
    public String getHolderNameBusiness() {
        return payload.getHolderNameBusiness();
    }
}
