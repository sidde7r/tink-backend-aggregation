package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.rpc;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public class RemittanceInformation {

    List<String> unstructured;
}
