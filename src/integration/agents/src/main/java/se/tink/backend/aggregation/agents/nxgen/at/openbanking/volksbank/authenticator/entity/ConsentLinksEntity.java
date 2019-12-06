package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.entity;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentLinksEntity {

    private ConsentLinkEntity scaRedirect;
    private ConsentLinkEntity status;
    private ConsentLinkEntity scaStatus;

    public String getScaRedirect() {
        return Optional.ofNullable(scaRedirect)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        VolksbankConstants.ErrorMessages.MISSING_SCA_URL))
                .getHref();
    }
}
