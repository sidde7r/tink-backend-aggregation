package se.tink.backend.aggregation.agents.nxgen.se.business.seb.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.authenticator.entities.DeviceIdentification;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.authenticator.entities.HardwareInformation;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.authenticator.entities.InitResult;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.fetcher.transactionalaccount.entities.ReservedTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.fetcher.transactionalaccount.entities.ReservedTransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.fetcher.transactionalaccount.entities.TransactionQuery;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.fetcher.transactionalaccount.entities.UpcomingTransactionEntity;
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

    @JsonProperty("RESULTO01")
    public InitResult initResult;

    @JsonProperty("PCBW4401")
    public List<AccountEntity> accountEntities;

    @JsonProperty("PCBW4342")
    public List<TransactionEntity> transactions;

    @JsonProperty("PCBW4341")
    public TransactionQuery transactionQuery;

    @JsonProperty("PCBW4311")
    public List<ReservedTransactionEntity> reservedTransactions;

    @JsonProperty("PCBW1361")
    public List<UpcomingTransactionEntity> upcomingTransactions;

    @JsonProperty("PCBW431Z")
    public ReservedTransactionQuery pendingTransactionQuery;
}
