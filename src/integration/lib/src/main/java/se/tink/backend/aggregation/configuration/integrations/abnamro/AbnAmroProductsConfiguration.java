package se.tink.backend.aggregation.configuration.integrations.abnamro;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

/** Default values for saving and payment accounts and credit cards disabled */
public class AbnAmroProductsConfiguration {
    private static final ImmutableList<Integer> SAVINGS_ACCOUNT_IDS = ImmutableList.of(5, 8);
    private static final ImmutableList<Integer> PAYMENTS_ACCOUNT_IDS = ImmutableList.of(13, 25);

    @JsonProperty
    private AbnAmroProductConfiguration savings =
            new AbnAmroProductConfiguration(
                    SAVINGS_ACCOUNT_IDS, ImmutableList.of("SAVINGS_ACCOUNTS"));

    @JsonProperty
    private AbnAmroProductConfiguration payments =
            new AbnAmroProductConfiguration(
                    PAYMENTS_ACCOUNT_IDS, ImmutableList.of("PAYMENT_ACCOUNTS"));

    @JsonProperty private AbnAmroProductConfiguration creditCards;

    public AbnAmroProductConfiguration getSavingProducts() {
        return savings;
    }

    public AbnAmroProductConfiguration getPaymentProducts() {
        return payments;
    }

    public AbnAmroProductConfiguration getCreditCardProducts() {
        return creditCards;
    }
}
