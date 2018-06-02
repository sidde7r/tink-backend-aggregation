package se.tink.backend.aggregation.agents.utils.httpclient;

import se.tink.backend.aggregation.agents.AbstractAgent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public abstract class HttpClientAgent extends AbstractAgent {
	protected HttpClientAgent(CredentialsRequest request, AgentContext context) {
		super(request, context);
	}
}
