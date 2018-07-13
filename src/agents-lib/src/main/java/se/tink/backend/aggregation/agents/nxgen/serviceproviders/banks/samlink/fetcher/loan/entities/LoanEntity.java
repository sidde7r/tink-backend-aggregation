package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.Links;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanEntity extends LinkEntity {
    private String loanPurpose;
    private String loanNumber;
    private AmountEntity balance;
    private Links links;

    public String getLoanPurpose() {
        return loanPurpose;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public List<LinkEntity> getLinks() {
        return links;
    }

    @JsonIgnore
    public Optional<String> getDetailsLink() {
        return Optional.ofNullable(links)
                .map(link -> link.findLink(SamlinkConstants.LinkRel.DETAILS))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(LinkEntity::getHref);
    }
}
