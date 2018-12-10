package se.tink.backend.aggregation.agents.utils.authentication.encap;

public interface EncapMessageService {

    String executeActivationExchange();
    String finishActivation(String activationSessionId, String samlObject);
    String executeAuthenticationExchange();
    String finishAuthentication(String samlObject);
}
