package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback.CmcicCallbackStatus.ERROR;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback.CmcicCallbackStatus.MULTIPLE_MATCH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback.CmcicCallbackStatus.SUCCESS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback.CmcicCallbackStatus.UNKNOWN;

import java.util.EnumMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicRepository;

@Slf4j
public class CmcicCallbackInterpreter {

    private final CmcicCallbackHandlingStrategy defaultStrategy;
    private final Map<CmcicCallbackStatus, CmcicCallbackHandlingStrategy> statusToStrategyMapping;
    private final CmcicCallbackDataFactory cmcicCallbackDataFactory;

    public CmcicCallbackInterpreter(CmcicRepository cmicRepostiory) {
        this(new CmcicCallbackUnknownHandler(), cmicRepostiory);
    }

    private CmcicCallbackInterpreter(
            CmcicCallbackHandlingStrategy defaultStrategy, CmcicRepository cmicRepostiory) {
        this(
                defaultStrategy,
                prepareCallbackMapping(defaultStrategy, cmicRepostiory),
                new CmcicCallbackDataFactory());
    }

    public CmcicCallbackInterpreter(
            CmcicCallbackHandlingStrategy defaultStrategy,
            Map<CmcicCallbackStatus, CmcicCallbackHandlingStrategy> statusToStrategyMapping,
            CmcicCallbackDataFactory cmcicCallbackDataFactory) {
        this.defaultStrategy = defaultStrategy;
        this.statusToStrategyMapping = statusToStrategyMapping;
        this.cmcicCallbackDataFactory = cmcicCallbackDataFactory;
    }

    private static EnumMap<CmcicCallbackStatus, CmcicCallbackHandlingStrategy>
            prepareCallbackMapping(
                    CmcicCallbackHandlingStrategy defaultStrategy, CmcicRepository cmicRepostiory) {
        EnumMap<CmcicCallbackStatus, CmcicCallbackHandlingStrategy>
                cmcicCallbackStatusObjectEnumMap = new EnumMap<>(CmcicCallbackStatus.class);
        CmcicCallbackHandlingStrategy cmcicCallbackSuccessStrategy =
                new UnexpectedDataLogging(new CmcicCallbackSuccessHandler(cmicRepostiory));
        CmcicCallbackHandlingStrategy cmcicCallbackErrorStrategy =
                new UnexpectedDataLogging(CmcicCallbackErrorHandler.create());
        cmcicCallbackStatusObjectEnumMap.put(SUCCESS, cmcicCallbackSuccessStrategy);
        cmcicCallbackStatusObjectEnumMap.put(ERROR, cmcicCallbackErrorStrategy);
        cmcicCallbackStatusObjectEnumMap.put(MULTIPLE_MATCH, defaultStrategy);
        cmcicCallbackStatusObjectEnumMap.put(UNKNOWN, defaultStrategy);
        return cmcicCallbackStatusObjectEnumMap;
    }

    public void interpretCallbackData(Map<String, String> callbackData) throws PaymentException {
        CmcicCallbackData cmcicCallbackData =
                cmcicCallbackDataFactory.fromCallbackData(callbackData);
        statusToStrategyMapping
                .getOrDefault(cmcicCallbackData.getStatus(), defaultStrategy)
                .handleCallback(cmcicCallbackData);
    }
}
