package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transferdestinations.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities.AliasesEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities.BalancesEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities.LimitsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transferdestinations.entities.AccountOwnersEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transferdestinations.entities.InterestConditionsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transferdestinations.entities.InterestsDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountDetailsResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private String accountInterest;
    private AccountOwnersEntity accountOwners;
    private List<AliasesEntity> aliases;
    private List<BalancesEntity> balances;
    private String bankgiroNumber;
    private String bban;
    private String bic;
    private String bicAddress;
    private boolean cardLinkedToTheAccount;
    private String creditLine;
    private String currency;
    private String iban;
    private InterestConditionsEntity interestConditions;
    private InterestsDetailsEntity interests;
    private LimitsEntity limits;
    private String name;
    private String ownerId;
    private String ownerName;
    private boolean paymentService;
    private String product;
    private String resourceId;
    private String status;
    private String statusDate;

    public boolean isPaymentService() {
        return paymentService;
    }
}
