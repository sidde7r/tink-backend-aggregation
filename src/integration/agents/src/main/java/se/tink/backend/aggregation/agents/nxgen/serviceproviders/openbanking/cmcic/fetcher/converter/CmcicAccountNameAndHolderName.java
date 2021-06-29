package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.converter;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CmcicAccountNameAndHolderName {
    private String accountName;
    private String holderName;
}
