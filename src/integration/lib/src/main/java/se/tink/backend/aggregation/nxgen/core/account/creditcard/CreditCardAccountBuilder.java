package se.tink.backend.aggregation.nxgen.core.account.creditcard;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import se.tink.backend.aggregation.nxgen.core.account.AccountBuilder;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.WithFlagPolicyStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.creditcard.CreditCardBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.creditcard.CreditCardDetailsStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.libraries.account.enums.AccountFlag;

public class CreditCardAccountBuilder extends AccountBuilder<CreditCardAccount, CreditCardBuildStep>
        implements CreditCardDetailsStep<CreditCardBuildStep>,
                CreditCardBuildStep,
                WithFlagPolicyStep<WithIdStep<CreditCardBuildStep>, AccountTypeMapper> {

    private CreditCardModule cardModule;

    @Override
    public WithFlagPolicyStep<WithIdStep<CreditCardBuildStep>, AccountTypeMapper> withCardDetails(
            CreditCardModule cardDetails) {
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
        return new CreditCardAccount(this, cardModule);
    }

    @Override
    public WithIdStep<CreditCardBuildStep> withFlagsFrom(AccountTypeMapper mapper, String typeKey) {
        Preconditions.checkNotNull(mapper, "Mapper must not be null");
        accountFlags.addAll(mapper.getItems(typeKey));
        return this;
    }

    @Override
    public WithIdStep<CreditCardBuildStep> withInferredAccountFlags() {
        return this;
    }

    @Override
    public WithIdStep<CreditCardBuildStep> withPaymentAccountFlag() {
        accountFlags.add(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        return this;
    }

    @Override
    public WithIdStep<CreditCardBuildStep> withFlags(AccountFlag... flags) {
        Preconditions.checkNotNull(flags, "Flags array must not be null.");
        accountFlags.addAll(Arrays.asList(flags));
        return this;
    }

    @Override
    public WithIdStep<CreditCardBuildStep> withoutFlags() {
        return this;
    }
}
