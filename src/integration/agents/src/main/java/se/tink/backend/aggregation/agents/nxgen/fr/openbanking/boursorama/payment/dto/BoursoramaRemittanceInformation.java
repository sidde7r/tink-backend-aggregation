package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class BoursoramaRemittanceInformation {

    private List<String> unstructured;

    public static BoursoramaRemittanceInformation of(List<String> remittanceInformation) {
        return new BoursoramaRemittanceInformation(remittanceInformation);
    }
}
