package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.cofidis;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.aggregation.agents.AgentContext;

public class CofidisAgent extends SibsBaseAgent {

  public CofidisAgent(
      CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
    super(request, context, signatureKeyPair);
  }

  @Override
  protected String getIntegrationName() {
    return CofidisConstants.INTEGRATION_NAME;
  }

}
