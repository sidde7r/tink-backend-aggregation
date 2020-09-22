package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@RequiredArgsConstructor
public class CheckBeneficiaryRequest {

    private final String label;
    private final String beneficiaryFirstname;
    private final String beneficiaryLastname;
    private final String iban;
    private final String bank;
    private final String isOwned = "0";
}
