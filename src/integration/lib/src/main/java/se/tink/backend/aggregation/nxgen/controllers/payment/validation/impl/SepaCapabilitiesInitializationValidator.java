package se.tink.backend.aggregation.nxgen.controllers.payment.validation.impl;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentcapabilities.CapabilitiesExtractor;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.nxgen.controllers.payment.validation.PaymentInitializationValidator;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@RequiredArgsConstructor
@Slf4j
// This implementation of a validator only checks very basic aspects of incoming agent, scheme and
// service, in relation to PisCapabilities declared by the agent.
public class SepaCapabilitiesInitializationValidator implements PaymentInitializationValidator {

    private static final ImmutableMap<PisCapability, Pair<PaymentServiceType, PaymentScheme>>
            CAPABILITY_SERVICE_SCHEME =
                    ImmutableMap.<PisCapability, Pair<PaymentServiceType, PaymentScheme>>builder()
                            .put(
                                    PisCapability.PIS_SEPA_RECURRING_PAYMENTS,
                                    Pair.of(
                                            PaymentServiceType.PERIODIC,
                                            PaymentScheme.SEPA_CREDIT_TRANSFER))
                            .put(
                                    PisCapability.PIS_SEPA_ICT_RECURRING_PAYMENTS,
                                    Pair.of(
                                            PaymentServiceType.PERIODIC,
                                            PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER))
                            .put(
                                    PisCapability.SEPA_CREDIT_TRANSFER,
                                    Pair.of(
                                            PaymentServiceType.SINGLE,
                                            PaymentScheme.SEPA_CREDIT_TRANSFER))
                            .put(
                                    PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
                                    Pair.of(
                                            PaymentServiceType.SINGLE,
                                            PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER))
                            .build();

    private final Class<? extends Agent> klass;
    private final MarketCode executedForMarketCode;

    @Override
    public void throwIfNotPossibleToInitialize(Payment payment) {
        // We exit without throwing anything in case of weird, unexpected situations
        // This is because I do not want to break/block/throw in case of any weird legacy
        // interactions I'm not aware of.
        // This validator is only made for simple SEPA stuff!
        Map<MarketCode, Set<PisCapability>> capabilities =
                CapabilitiesExtractor.readPisCapabilities(klass);

        PaymentServiceType serviceType = payment.getPaymentServiceType();
        PaymentScheme scheme = payment.getPaymentScheme();

        if (areAnyCapabilitiesPresentOnAgent(capabilities)
                && serviceAndSchemeOfExpectedTypes(serviceType, scheme)) {
            validateIfCapabilitiesSupportPayment(capabilities, serviceType, scheme);
        }
    }

    private boolean areAnyCapabilitiesPresentOnAgent(
            Map<MarketCode, Set<PisCapability>> capabilities) {
        if (capabilities.isEmpty() || capabilities.get(executedForMarketCode) == null) {
            log.warn(
                    "Executed payments with no PisCapabilities on agent, or none for relevant market, skipping.");
            return false;
        }
        return true;
    }

    private boolean serviceAndSchemeOfExpectedTypes(
            PaymentServiceType serviceType, PaymentScheme scheme) {
        if (serviceType == null || scheme == null) {
            log.warn("Either scheme or serviceType was null, skipping.");
            return false;
        }

        if (PaymentScheme.SEPA_CREDIT_TRANSFER != scheme
                && PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER != scheme) {
            log.warn("Non-sepa scheme encountered, skipping.");
            return false;
        }
        return true;
    }

    private void validateIfCapabilitiesSupportPayment(
            Map<MarketCode, Set<PisCapability>> capabilities,
            PaymentServiceType serviceType,
            PaymentScheme scheme) {
        Set<PisCapability> interestingCapabilitiesOnAgent =
                capabilities.get(executedForMarketCode).stream()
                        .filter(CAPABILITY_SERVICE_SCHEME::containsKey)
                        .collect(Collectors.toSet());

        if (interestingCapabilitiesOnAgent.isEmpty()) {
            log.warn(
                    "This agent does not support any SEPA capability! Why was this validator used? Skipping.");
            return;
        }

        boolean matchFound =
                interestingCapabilitiesOnAgent.stream()
                        .anyMatch(x -> isCapabilitySupportingThisPayment(x, serviceType, scheme));

        if (!matchFound) {
            throw new PaymentValidationException(InternalStatus.INVALID_PAYMENT_TYPE);
        }
    }

    private boolean isCapabilitySupportingThisPayment(
            PisCapability capability, PaymentServiceType serviceType, PaymentScheme paymentScheme) {
        Pair<PaymentServiceType, PaymentScheme> serviceSchemePair =
                CAPABILITY_SERVICE_SCHEME.get(capability);
        return serviceSchemePair != null
                && serviceSchemePair.getLeft() == serviceType
                && serviceSchemePair.getRight() == paymentScheme;
    }
}
