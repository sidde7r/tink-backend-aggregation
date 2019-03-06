package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AssetBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.FamilyBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.LiabilityBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.PositionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinancialDashboardResponse {
    private boolean hasMoreData;
    private List<PositionEntity> positions;
    private LiabilityBalanceEntity liabilityBalance;
    private boolean isEnrolled;
    private AssetBalanceEntity assetBalance;
    private List<FamilyBalanceEntity> familyBalances;

    public boolean isHasMoreData() {
        return hasMoreData;
    }

    public List<PositionEntity> getPositions() {
        return positions;
    }

    public LiabilityBalanceEntity getLiabilityBalance() {
        return liabilityBalance;
    }

    public boolean isIsEnrolled() {
        return isEnrolled;
    }

    public AssetBalanceEntity getAssetBalance() {
        return assetBalance;
    }

    public List<FamilyBalanceEntity> getFamilyBalances() {
        return familyBalances;
    }
}
