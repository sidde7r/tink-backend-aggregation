package se.tink.backend.aggregation.agents.utils.berlingroup.payment.helper;

import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.PathVariables;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentProduct;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentService;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.payment.rpc.Payment;

public class PaymentUrlUtil {

    public static URL fillCommonPaymentParams(URL url, Payment payment) {
        URL returnUrl = url;

        // Ternary ifs only needed because those method in enums are a bit too eager to assign one
        // of the values to `null` input.
        // That should be improved.
        returnUrl =
                fillParamIfExists(
                        returnUrl,
                        PathVariables.PAYMENT_SERVICE,
                        payment.getPaymentServiceType() != null
                                ? PaymentService.getPaymentService(payment.getPaymentServiceType())
                                : null);
        returnUrl =
                fillParamIfExists(
                        returnUrl,
                        PathVariables.PAYMENT_PRODUCT,
                        payment.getPaymentScheme() != null
                                ? PaymentProduct.getPaymentProduct(payment.getPaymentScheme())
                                : null);
        returnUrl = fillParamIfExists(returnUrl, PathVariables.PAYMENT_ID, payment.getUniqueId());

        return returnUrl;
    }

    private static URL fillParamIfExists(URL url, String key, String value) {
        if (url.toString().contains("{" + key + "}") && value != null) {
            return url.parameter(key, value);
        } else {
            return url;
        }
    }
}
