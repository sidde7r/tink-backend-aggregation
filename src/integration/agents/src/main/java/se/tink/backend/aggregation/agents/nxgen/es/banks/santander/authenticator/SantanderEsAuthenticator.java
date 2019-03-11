package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.authenticator;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsXmlUtils;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SantanderEsAuthenticator implements PasswordAuthenticator {
    private static final Logger LOG = LoggerFactory.getLogger(SantanderEsAuthenticator.class);
    private static final AggregationLogger AGGREGATION_LOGGER = new AggregationLogger(SantanderEsAuthenticator.class);

    private final SantanderEsApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SantanderEsAuthenticator(SantanderEsApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {
        String responseString;
        try {
            responseString = apiClient.authenticateCredentials(username, password);
        } catch (HttpResponseException e) {
            String errorCode = SantanderEsXmlUtils
                    .getTagNodeFromSoapString(e.getResponse().getBody(String.class),
                            SantanderEsConstants.NodeTags.CODIGO_ERROR)
                    .getFirstChild()
                    .getTextContent()
                    .trim()
                    .toUpperCase();

            switch (errorCode) {
            case SantanderEsConstants.ErrorCodes.INCORRECT_CREDENTIALS:
                throw new LoginException(LoginError.INCORRECT_CREDENTIALS);
            default:
                throw e;
            }
        }

        // Parse token credential and add it to api client to be used for future requests
        Node tokenCredentialNode = SantanderEsXmlUtils.getTagNodeFromSoapString(
                responseString, SantanderEsConstants.NodeTags.TOKEN_CREDENTIAL);

        String tokenCredential = Preconditions.checkNotNull(
                tokenCredentialNode.getFirstChild().getTextContent(),
                "Could not parse token credentials.");
        apiClient.setTokenCredential(tokenCredential);

        responseString = apiClient.login();

        // Login response contain users accounts, save to session storage to use for later fetching
        Node loginResponseNode = SantanderEsXmlUtils.getTagNodeFromSoapString(
                responseString, SantanderEsConstants.NodeTags.METHOD_RESULT);

        // log unknown engagements to see what more we can find
//        logUnknownEngagementNodes(loginResponseNode);

        // logging whole response
//        LOG.info("es_santander_all " + responseString);
        AGGREGATION_LOGGER.infoExtraLong(responseString, SantanderEsConstants.Tags.ES_SANTANDER_ALL);

        String loginResponseString = SantanderEsXmlUtils.convertToString(loginResponseNode);

        sessionStorage.put(SantanderEsConstants.Storage.LOGIN_RESPONSE, loginResponseString);

    }

    private void logUnknownEngagementNodes(Node loginResponseNode) {
        try {
            Node childNode = loginResponseNode.getFirstChild();

            StringBuilder unknownNodes = new StringBuilder("");
            while (childNode != null) {
                String nodeName = childNode.getNodeName();
                childNode = childNode.getNextSibling();
                if (SantanderEsConstants.LogMessages.KNOWN_NODES.contains(nodeName)) {
                    continue;
                }

                unknownNodes.append(nodeName);
                unknownNodes.append(", ");
            }

            if (unknownNodes.length() > 0) {
                LOG.info(String.format("%s: %s ", SantanderEsConstants.Tags.UNKNOWN_ENGAGEMENT_TYPE.toString(),
                        unknownNodes.toString()));
            }
        } catch (Exception e) {
            LOG.info("Could not log unknown engagement types");
        }
    }
}
