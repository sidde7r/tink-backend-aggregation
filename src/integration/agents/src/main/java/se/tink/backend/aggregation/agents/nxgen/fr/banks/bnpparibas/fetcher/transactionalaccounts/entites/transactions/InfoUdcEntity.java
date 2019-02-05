package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InfoUdcEntity {
    @JsonProperty("premierTauxRepartition")
    private int firstRateDistribution;
    @JsonProperty("deuxiemeTauxRepartition")
    private int secondRateDistribution;
    @JsonProperty("urlAiguillage")
    private String urlReferrals;
    @JsonProperty("titulaireConnecte")
    private ConnectedHolderEntity connectedHolder;
    @JsonProperty("familleCompte")
    private List<AccountGroupEntity> accountGroup;
    private int categorisationAuto;
    @JsonProperty("clientBD")
    private boolean clientbd;
    private int cslProduction;
    private int cslModification;
    @JsonProperty("indicBCapitalTitre")
    private int indicbCapitalTitle;
    private boolean indicServiceHomeConnecte;

    public int getFirstRateDistribution() {
        return firstRateDistribution;
    }

    public int getSecondRateDistribution() {
        return secondRateDistribution;
    }

    public String getUrlReferrals() {
        return urlReferrals;
    }

    public ConnectedHolderEntity getConnectedHolder() {
        return connectedHolder;
    }

    public List<AccountGroupEntity> getAccountGroup() {
        return accountGroup;
    }

    public int getCategorisationAuto() {
        return categorisationAuto;
    }

    public boolean isClientbd() {
        return clientbd;
    }

    public int getCslProduction() {
        return cslProduction;
    }

    public int getCslModification() {
        return cslModification;
    }

    public int getIndicbCapitalTitle() {
        return indicbCapitalTitle;
    }

    public boolean isIndicServiceHomeConnecte() {
        return indicServiceHomeConnecte;
    }
}
