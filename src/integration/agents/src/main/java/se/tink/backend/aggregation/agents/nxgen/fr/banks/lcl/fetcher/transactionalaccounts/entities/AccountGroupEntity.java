package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountGroupEntity {
    @JsonProperty("libelle")
    private String label;

    @JsonProperty("indice")
    private int index;

    @JsonProperty("listGroupe")
    private List<GroupListEntity> groupList;

    @JsonProperty("tagHtml")
    private String groupType;

    private boolean topCache;

    @JsonIgnore
    public boolean isCheckingAccountGroup() {
        return LclConstants.AccountTypes.CHECKING_ACCOUNT_TAG.equalsIgnoreCase(getGroupType());
    }

    public String getLabel() {
        return label;
    }

    public int getIndex() {
        return index;
    }

    public List<GroupListEntity> getGroupList() {
        return groupList;
    }

    public String getGroupType() {
        return groupType == null ? "" : groupType.trim();
    }

    public boolean isTopCache() {
        return topCache;
    }
}
