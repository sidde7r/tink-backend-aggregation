package se.tink.backend.aggregation.agents.abnamro.utils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Map;
import se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders.Adyen;
import se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders.Buckaroo;
import se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders.ClickAndBuy;
import se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders.GlobalCollect;
import se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders.Ingenico;
import se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders.Mollie;
import se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders.MultisafePay;
import se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders.Paydotnl;
import se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders.PaymentProvider;
import se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders.Sisow;
import se.tink.backend.aggregation.agents.abnamro.utils.paymentproviders.WorldPay;

public class AbnAmroPaymentProviderUtils {

    private static final ImmutableList<PaymentProvider> PAYMENT_PROVIDERS =
            ImmutableList.<PaymentProvider>builder()
                    .add(new Adyen())
                    .add(new Buckaroo())
                    .add(new ClickAndBuy())
                    .add(new GlobalCollect())
                    .add(new Mollie())
                    .add(new MultisafePay())
                    .add(new Paydotnl())
                    .add(new Sisow())
                    .add(new WorldPay())
                    .add(new Ingenico())
                    .build();

    public static ImmutableList<PaymentProvider> getPaymentProviders() {
        return PAYMENT_PROVIDERS;
    }

    public static String getPaymentProviderDescription(Map<String, String> descriptionParts) {

        String name = descriptionParts.get(AbnAmroUtils.DescriptionKeys.NAME);
        String description = descriptionParts.get(AbnAmroUtils.DescriptionKeys.DESCRIPTION);

        if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(description)) {
            return null;
        }

        for (PaymentProvider provider : PAYMENT_PROVIDERS) {
            if (provider.matches(name)) {

                String result = provider.getDescription(description);

                if (!Strings.isNullOrEmpty(result)) {
                    return result;
                }
            }
        }

        return null;
    }
}
