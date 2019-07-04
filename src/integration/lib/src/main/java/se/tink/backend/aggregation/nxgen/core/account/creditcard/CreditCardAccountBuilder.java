package se.tink.backend.aggregation.nxgen.core.account.creditcard;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.nxgen.core.account.AccountBuilder;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.creditcard.CreditCardBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.creditcard.CreditCardDetailsStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;

public class CreditCardAccountBuilder extends AccountBuilder<CreditCardAccount, CreditCardBuildStep>
        implements CreditCardDetailsStep<CreditCardBuildStep>, CreditCardBuildStep {

    private CreditCardModule cardModule;

    @Override
    public WithIdStep<CreditCardBuildStep> withCardDetails(CreditCardModule cardDetails) {
        Preconditions.checkNotNull(cardDetails, "Credit Card Details must not be null.");
        this.cardModule = cardDetails;
        return this;
    }

    @Override
    protected CreditCardBuildStep buildStep() {
        return this;
    }

    @Override
    public CreditCardAccount build() {
        Preconditions.checkNotNull(getBalanceModule(), "Balance Module must not be null.");
        Preconditions.checkState(
                getBalanceModule().getAvailableCredit().isPresent(),
                "A credit card must have a credit.");

        return new CreditCardAccount(this);
    }

    public CreditCardModule getCardModule() {
        return cardModule;
    }
}
