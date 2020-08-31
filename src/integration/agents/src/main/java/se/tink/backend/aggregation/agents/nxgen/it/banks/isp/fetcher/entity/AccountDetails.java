package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountDetails {
    private String iban;
}
