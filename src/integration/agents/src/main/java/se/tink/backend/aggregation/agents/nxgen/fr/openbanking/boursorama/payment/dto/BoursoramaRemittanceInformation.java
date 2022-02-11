package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BoursoramaRemittanceInformation {

    private final List<String> unstructured;

    public static BoursoramaRemittanceInformation of(List<String> remittanceInformation) {
        return new BoursoramaRemittanceInformation(remittanceInformation);
    }
}
