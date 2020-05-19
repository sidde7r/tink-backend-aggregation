package se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter.AccountNumberToIbanConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter.SparbankenSydAccountNumberToIbanConverter;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

/*
 * Configure market specific client, this is SE in OXFORD
 */
public class SdcSeWithIbanConverterAgent extends SdcSeAgent {

    public SdcSeWithIbanConverterAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected AccountNumberToIbanConverter getIbanConverter() {
        return new SparbankenSydAccountNumberToIbanConverter();
    }
}
