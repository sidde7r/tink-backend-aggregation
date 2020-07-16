package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DatesEntity {
    private String date;
    private String label;
    private String type;

    public String getLabel() {
        return label;
    }

    public LocalDate getDate() {
        return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    boolean isPerformedOnDate() {
        return type.equals(BoursoramaConstants.Transaction.PERFORMED_ON_LABEL);
    }
}
