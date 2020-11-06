package se.tink.backend.integration.agent_data_availability_tracker.client.serialization;

import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Loan;

public class LoanTrackingSerializer extends TrackingMapSerializer {
    private static final String LOAN_ENTITY_NAME = "Loan";
    private final Loan loan;

    public LoanTrackingSerializer(Loan loan) {
        super(String.format(LOAN_ENTITY_NAME + "<%s>", String.valueOf(loan.getType())));
        this.loan = loan;
        Optional.of(loan)
                .map(Loan::getLoanDetails)
                .ifPresent(ld -> addChild("loanDetails", new LoanDetailsTrackingSerializer(ld)));
    }

    @Override
    protected TrackingList populateTrackingMap(TrackingList.Builder list) {

        list.putRedacted("initialBalance", loan.getInitialBalance())
                .putRedacted("numberOfMonthsBound", loan.getNumMonthsBound())
                .putRedacted("interest", loan.getInterest())
                .putRedacted("balance", loan.getBalance())
                .putRedacted("amortized", loan.getAmortized())
                .putRedacted("monthlyAmortization", loan.getMonthlyAmortization())
                .putRedacted("name", loan.getName())
                .putRedacted("providerName", loan.getProviderName())
                .putRedacted("loanNumber", loan.getLoanNumber())
                .putRedacted("nextDayOfTermChange", loan.getNextDayOfTermsChange())
                .putRedacted("updated", loan.getUpdated())
                .putListed("type", loan.getType());

        return list.build();
    }
}
