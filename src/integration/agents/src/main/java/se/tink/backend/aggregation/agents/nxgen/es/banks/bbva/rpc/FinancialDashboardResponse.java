package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc;

import io.vavr.collection.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.FamilyBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.PositionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinancialDashboardResponse {
    private boolean hasMoreData;
    private List<PositionEntity> positions;
    private AmountEntity liabilityBalance;
    private boolean isEnrolled;
    private AmountEntity assetBalance;
    private List<FamilyBalanceEntity> familyBalances;

    public boolean isHasMoreData() {
        return hasMoreData;
    }

    public List<PositionEntity> getPositions() {
        return positions;
    }

    public AmountEntity getLiabilityBalance() {
        return liabilityBalance;
    }

    public boolean isIsEnrolled() {
        return isEnrolled;
    }

    public AmountEntity getAssetBalance() {
        return assetBalance;
    }

    public List<FamilyBalanceEntity> getFamilyBalances() {
        return familyBalances;
    }
}
