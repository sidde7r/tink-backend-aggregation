package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContactInfoResponse {
    private List<EmailAddressEntity> emailAddresses;
    private List<LinksEntity> links;
}
