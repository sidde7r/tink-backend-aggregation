package se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat;

import com.google.common.base.Objects;
import com.google.common.util.concurrent.Uninterruptibles;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.model.CollectBankIdRequest;
import se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.model.CollectBankIdResponse;
import se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.model.CompleteBankIdResponse;
import se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.model.ErrorCode;
import se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.model.InitiateBankIdRequest;
import se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.model.InitiateBankIdResponse;
import se.tink.backend.aggregation.agents.utils.authentication.bankid.signicat.model.StatusMessage;

public class SignicatBankIdAuthenticator implements Runnable {
    private static final int AUTHENTICATION_BANKID_TIMEOUT = 120;
    private static final String AUTHENTICATION_BANKID_URL =
            "https://id.signicat.com/std/method/tink.se?id=sbid-inapp:default:sv&target=https%3A%2F%2Fwww.tink.se%2Fapi%2Fv1%2Fauthentication%2Fsaml";
    private static final boolean DEBUG = false;
    private static final String API_KEY = "B+=3e6uSUjeThen";
    private static final Logger log = LoggerFactory.getLogger(SignicatBankIdAuthenticator.class);

    private final SignicatBankIdHandler handler;
    private final String socialSecurityNumber;
    private final String credentialsId;
    private final String userId;

    public SignicatBankIdAuthenticator(
            String socialSecurityNumber,
            String userId,
            String credentialsId,
            SignicatBankIdHandler handler) {
        this.socialSecurityNumber = socialSecurityNumber;
        this.userId = userId;
        this.credentialsId = credentialsId;
        this.handler = handler;
    }

    /** Helper method to create a Jersey client. */
    private static Client createClient() {
        ApacheHttpClient4Config clientConfig = new DefaultApacheHttpClient4Config();

        ApacheHttpClient4 client = ApacheHttpClient4.create(clientConfig);
        client.setChunkedEncodingSize(null);

        if (DEBUG) {
            try {
                client.addFilter(new LoggingFilter(new PrintStream(System.out, true, "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                log.error("Could not add logging filter");
            }
        }

        return client;
    }

    /** Helper method to create a Jersey request. */
    private static Builder createClientRequest(String url, Client client) {
        return client.resource(url).accept(MediaType.APPLICATION_JSON);
    }

    @Override
    public void run() {
        Client client = createClient();

        // Initiate a BankId authentication server-side.

        log.info(userId, credentialsId, "Initializing BankID authentication.");

        InitiateBankIdRequest orderBankIdRequest = new InitiateBankIdRequest();

        orderBankIdRequest.setSubject(socialSecurityNumber);
        orderBankIdRequest.setApiKey(API_KEY);

        InitiateBankIdResponse initiateBankIdResponse =
                createClientRequest(AUTHENTICATION_BANKID_URL, client)
                        .type(MediaType.APPLICATION_JSON)
                        .post(InitiateBankIdResponse.class, orderBankIdRequest);

        if (initiateBankIdResponse.getError() != null) {
            log.warn(
                    userId,
                    credentialsId,
                    "BankID authentication error: " + initiateBankIdResponse.getError().getCode());
            handler.onUpdateStatus(SignicatBankIdStatus.AUTHENTICATION_ERROR);
        }

        // Validate authentication.

        CollectBankIdRequest collectBankIdRequest = new CollectBankIdRequest();
        collectBankIdRequest.setOrderRef(initiateBankIdResponse.getOrderRef());

        CollectBankIdResponse collectBankIdResponse;

        // Poll BankID status periodically until the process is complete.

        for (int i = 0; i < AUTHENTICATION_BANKID_TIMEOUT; i++) {
            collectBankIdResponse =
                    createClientRequest(initiateBankIdResponse.getCollectUrl(), client)
                            .type(MediaType.APPLICATION_JSON)
                            .post(CollectBankIdResponse.class, collectBankIdRequest);

            String status = collectBankIdResponse.getProgressStatus();

            if (collectBankIdResponse.getError() != null) {
                String errorCode = collectBankIdResponse.getError().getCode();
                String logMessage =
                        String.format(
                                "BankID failed with progress status: %s, error status: %s and message: %s",
                                status, errorCode, collectBankIdResponse.getError().getMessage());

                if (Objects.equal(errorCode, ErrorCode.CANCELLED)) {
                    log.info(userId, credentialsId, logMessage);
                } else {
                    log.error(userId, credentialsId, logMessage);
                }

                handler.onUpdateStatus(SignicatBankIdStatus.AUTHENTICATION_ERROR);
                return;
            }

            switch (status) {
                case StatusMessage.OUTSTANDING_TRANSACTION:
                case StatusMessage.USER_SIGN:
                case StatusMessage.STARTED:
                    log.info(
                            userId,
                            credentialsId,
                            "BankID authentication is not yet complete, with status " + status);
                    handler.onUpdateStatus(SignicatBankIdStatus.AWAITING_BANKID_AUTHENTICATION);

                    Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                    continue;
                case StatusMessage.RETRY:
                case StatusMessage.NO_CLIENT:
                    log.info(
                            userId,
                            credentialsId,
                            "BankID authentication is not yet complete, with status " + status);
                    handler.onUpdateStatus(SignicatBankIdStatus.AUTHENTICATION_ERROR);
                    return;
                case StatusMessage.COMPLETE:
                    log.info(
                            userId,
                            credentialsId,
                            "BankID authentication done, with status " + status);
                    createClientRequest(collectBankIdResponse.getCompleteUrl(), client)
                            .get(CompleteBankIdResponse.class);

                    handler.onUpdateStatus(SignicatBankIdStatus.AUTHENTICATED);
                    return;
                default:
                    log.info(
                            userId,
                            credentialsId,
                            "BankID authentication is not yet complete, with status " + status);
                    handler.onUpdateStatus(SignicatBankIdStatus.AUTHENTICATION_ERROR);
                    throw new RuntimeException("unknown status: " + status);
            }
        }

        handler.onUpdateStatus(SignicatBankIdStatus.AUTHENTICATION_ERROR);
    }
}
