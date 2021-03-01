package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Setter
@Getter
@JsonObject
public class BasketEntity {

    List<PaymentIdEntity> transactions;
}
