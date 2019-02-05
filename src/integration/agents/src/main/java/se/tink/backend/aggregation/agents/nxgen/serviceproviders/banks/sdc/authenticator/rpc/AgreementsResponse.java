package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreements;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgreementsResponse extends ArrayList<SdcAgreement> {

    @JsonIgnore
    public SessionStorageAgreements toSessionStorageAgreements() {

        SessionStorageAgreements agreements = new SessionStorageAgreements();
        agreements.addAll(stream()
                .map(SdcAgreement::getEntityKey)
                .map(SessionStorageAgreement::new)
                .collect(Collectors.toList()));

        return agreements;
    }
}
