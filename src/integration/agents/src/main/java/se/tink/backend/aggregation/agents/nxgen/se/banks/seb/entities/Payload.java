package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.entities.DeviceIdentification;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.entities.HardwareInformation;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.entities.InitResult;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.ReservedTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.ReservedTransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.TransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.UpcomingTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class Payload {

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
    public List<AccountEntity> getAccountEntities() {
        Preconditions.checkNotNull(vodb);
        return vodb.accountEntities;
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
}
