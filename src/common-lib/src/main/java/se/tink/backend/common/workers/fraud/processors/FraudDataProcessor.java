package se.tink.backend.common.workers.fraud.processors;

import se.tink.backend.common.workers.fraud.FraudDataProcessorContext;


public abstract class FraudDataProcessor {

    public abstract void process(FraudDataProcessorContext context);
}
