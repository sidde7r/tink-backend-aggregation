package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.credit.entity;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BBVAConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class Detail {
    private List<ImagesItem> images;
    private List<SpecificAmountsItem> specificAmounts;
    private PhysicalSupport physicalSupport;
    private List<ActivationsItem> activations;
    private List<IndicatorsItem> indicators;
    private List<RewardsItem> rewards;
    private String expirationDate;
    private String agreementContract;

    public Amount getBalance() {
        return specificAmounts
                .stream()
                .filter(x -> BBVAConstants.VALUES.CURRENT_BALANCE.equalsIgnoreCase(x.getId()))
                .findFirst()
                .get()
                .getAmounts()
                .get(0)
                .toTinkAmount();
    }

    public Amount getAvailableCredit() {
        return specificAmounts
                .stream()
                .filter(x -> BBVAConstants.VALUES.AVAILABLE_BALANCE.equalsIgnoreCase(x.getId()))
                .findFirst()
                .get()
                .getAmounts()
                .get(0)
                .toTinkAmount();
    }
}
