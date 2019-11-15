package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.loan;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaTypeMappers.LOAN_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.control.Option;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;

@JsonObject
public class ConsumerLoanEntity extends BaseLoanEntity {
    private static final Logger log = LoggerFactory.getLogger(ConsumerLoanEntity.class);

    private AmountEntity awardedAmount;
    private AmountEntity pendingamount;
    private LoanTypeEntity loanType;
    private String digit;

    @JsonIgnore
    private LoanDetails.Type getTinkLoanType() {
        return Option.of(loanType)
                .map(LoanTypeEntity::getId)
                .map(LOAN_TYPE_MAPPER::translate)
                .flatMap(Option::ofOptional)
                .getOrElse(LoanDetails.Type.OTHER);
    }

    @JsonIgnore
    public Optional<LoanAccount> toTinkConsumerLoan(LoanDetailsResponse loanDetails) {

        Optional<LoanModule> loanModule =
                loanDetails.getLoanModuleWithTypeAndLoanNumber(getTinkLoanType(), digit);

        return loanModule.map(
                module ->
                        LoanAccount.nxBuilder()
                                .withLoanDetails(module)
                                .withId(getIdModuleWithUniqueIdentifier(digit))
                                .setApiIdentifier(digit)
                                .build());
    }
}
