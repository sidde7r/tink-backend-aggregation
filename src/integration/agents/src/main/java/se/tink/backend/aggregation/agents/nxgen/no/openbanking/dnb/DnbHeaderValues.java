package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DnbHeaderValues {

    private String psuId;
    private String redirectUrl;
    private String userIp;
}
