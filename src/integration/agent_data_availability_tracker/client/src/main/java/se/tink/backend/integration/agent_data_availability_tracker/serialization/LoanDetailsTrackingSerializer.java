package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.LoanDetails;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingList;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingMapSerializer;

public class LoanDetailsTrackingSerializer extends TrackingMapSerializer {
    private static final String LOAN_DETAILS = "LoanDetails";
    private final LoanDetails loanDetails;

    public LoanDetailsTrackingSerializer(LoanDetails loanDetails) {
        super(LOAN_DETAILS);
        this.loanDetails = loanDetails;
    }

    @Override
    protected TrackingList populateTrackingMap(TrackingList.Builder list) {
        list.putListed("coApplicants", loanDetails.getCoApplicant())
                .putRedacted("loanSecurity", loanDetails.getLoanSecurity());
        String collect =
                Optional.ofNullable(loanDetails.getApplicants()).orElseGet(Collections::emptyList)
                        .stream()
                        .collect(Collectors.joining(","));
        list.putRedacted("applicants", collect);
        return list.build();
    }
}
