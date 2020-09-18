package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.executor.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConfirmBeneficiaryResponse {
    private String success;
    private AccountInformation account;
}
