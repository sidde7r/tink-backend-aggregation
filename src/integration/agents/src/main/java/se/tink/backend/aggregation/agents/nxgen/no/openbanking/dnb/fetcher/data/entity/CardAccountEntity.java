package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CardAccountEntity {

    private String currency;
    private String maskedPan;
    private String name;
    private String resourceId;

    private List<Balance> balances;
}
