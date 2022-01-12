package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ArkeaPostalAddressEntity {

    private String country;
    private List<String> addressLine;
}
