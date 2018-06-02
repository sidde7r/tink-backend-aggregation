package se.tink.backend.common.repository.cassandra.DAO;

import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.cassandra.LoanDetailsRepository;
import se.tink.backend.core.Loan;

public class LoanDAO {

    private final LoanDataRepository loanDataRepository;
    private final LoanDetailsRepository loanDetailsRepository;

    @Inject
    public LoanDAO(LoanDataRepository loanDataRepository, LoanDetailsRepository loanDetailsRepository) {
        this.loanDataRepository = loanDataRepository;
        this.loanDetailsRepository = loanDetailsRepository;
    }

    public boolean saveIfUpdated(Loan loan) {
        
        boolean wasUpdated = false;
        
        if (loanDataRepository.hasBeenUpdated(loan)) {

            // Assure that the loan we are about to save have an interest rate.
            // If there is no interest rate we want to add the interest rate from
            // the latest saved loan (can also be null).
            // This was added due to issue with Danske bank, https://github.com/tink-ab/tink-backend/pull/4725
            Loan mostRecentLoan = loanDataRepository.findMostRecentOneByAccountId(loan.getAccountId());

            if (mostRecentLoan != null) {
                if (loan.getInterest() == null) {
                    loan.setInterest(mostRecentLoan.getInterest());
                }

                if (!loan.isUserModifiedType() && mostRecentLoan.isUserModifiedType()) {
                    loan.setUserModifiedType(true);
                    loan.setType(mostRecentLoan.getType());
                }
            }

            loanDataRepository.save(loan);
            wasUpdated = true;
        }

        if (loan.getLoanDetails() == null) {
            return wasUpdated;
        }

        // Update the record of the loanDetails if they have been updated
        // It's not necessary to set wasUpdate if just loan details are updated
        loan.getLoanDetails().setAccountId(loan.getAccountId());
        if (loanDetailsRepository.hasBeenUpdated(loan.getLoanDetails())) {
            loanDetailsRepository.save(loan.getLoanDetails());
        }
        
        return wasUpdated;
    }

    public void deleteByAccountId(String accountId) {
        deleteByAccountId(UUIDUtils.fromTinkUUID(accountId));
    }

    public void deleteByAccountId(UUID accountId) {
        loanDataRepository.deleteByAccountId(accountId);
        loanDetailsRepository.deleteByAccountId(accountId);
    }

    public List<Loan> getLoanDataByAccountId(String accountId) {

        return loanDataRepository.findAllByAccountId(accountId).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
