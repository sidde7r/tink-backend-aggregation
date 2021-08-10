package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class ContactDataResponse {

    private String alternativePhone;
    private String emailCompanyAddress;
    private String emailPersonalAddress;
    private String mobilePhone;
}
