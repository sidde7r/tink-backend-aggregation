package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GroupListEntity {
    @JsonProperty("idGroupe")
    private String groupId;

    @JsonProperty("libelle")
    private String label;

    @JsonProperty("indice")
    private int index;

    @JsonProperty("groupeDefault")
    private boolean defaultGroup;

    @JsonProperty("listCompte")
    private List<AccountEntity> accountList;

    private boolean topCache;

    public String getGroupId() {
        return groupId;
    }

    public String getLabel() {
        return label;
    }

    public int getIndex() {
        return index;
    }

    public boolean isDefaultGroup() {
        return defaultGroup;
    }

    public List<AccountEntity> getAccountList() {
        return accountList;
    }

    public boolean isTopCache() {
        return topCache;
    }
}
