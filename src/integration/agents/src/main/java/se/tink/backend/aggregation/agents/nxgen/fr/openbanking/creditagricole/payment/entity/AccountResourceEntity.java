package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountResourceEntity {
    @JsonProperty("resourceId")
    private String resourceId = null;

    @JsonProperty("accountId")
    private PsuAccountIdentificationEntity accountId = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("linkedAccount")
    private String linkedAccount = null;

    @JsonProperty("cashAccountType")
    private CashAccountTypeEnumEntity cashAccountType = null;

    @JsonProperty("balances")
    private List<BalanceResourceEntity> balances = new ArrayList<BalanceResourceEntity>();

    @JsonProperty("psuStatus")
    private String psuStatus = null;

    @JsonProperty("_links")
    private AccountLinksEntity links = null;
}
