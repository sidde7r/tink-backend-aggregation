package se.tink.backend.main.resources;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response.Status;
import se.tink.backend.api.AuthorizationService;
import se.tink.backend.api.WebHookService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.repository.mysql.main.OAuth2AuthorizationRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.common.repository.mysql.main.OAuth2WebHookRepository;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.backend.core.oauth2.OAuth2Authorization;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.core.oauth2.OAuth2WebHook;
import se.tink.backend.main.mappers.CoreOauth2WebHookMapper;
import se.tink.backend.rpc.OAuth2WebHookResponse;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.http.utils.HttpResponseHelper;

@Path("/api/v1/authorization")
public class AuthorizationServiceResource implements AuthorizationService, WebHookService {
    private static final LogUtils log = new LogUtils(AuthorizationServiceResource.class);
    private final OAuth2AuthorizationRepository authorizationRepository;
    private final OAuth2ClientRepository oAuth2ClientRepository;
    private final OAuth2WebHookRepository webHookRepository;
    private final HttpResponseHelper httpResponseHelper;

    public AuthorizationServiceResource(ServiceContext context) {
        this.authorizationRepository = context.getRepository(OAuth2AuthorizationRepository.class);
        this.oAuth2ClientRepository = context.getRepository(OAuth2ClientRepository.class);
        this.webHookRepository = context.getRepository(OAuth2WebHookRepository.class);
        this.httpResponseHelper = new HttpResponseHelper(log);
    }

    @Override
    public void delete(AuthenticatedUser authenticatedUser, String id) {
        OAuth2Authorization authorization = authorizationRepository.findOne(id);

        if (authorization == null) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        if (!Objects.equals(authorization.getUserId(), authenticatedUser.getUser().getId())) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        authorizationRepository.delete(authorization);
    }

    @Override
    public OAuth2Authorization get(AuthenticatedUser authenticatedUser, String id) {
        OAuth2Authorization authorization = authorizationRepository.findOne(id);

        if (authorization == null) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        if (!Objects.equals(authorization.getUserId(), authenticatedUser.getUser().getId())) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        return authorization;
    }

    @Override
    public List<OAuth2Authorization> list(AuthenticatedUser authenticatedUser) {
        return authorizationRepository.findByUserId(authenticatedUser.getUser().getId());
    }

    @Override
    public OAuth2WebHookResponse listHooks(AuthenticatedUser authenticatedUser) {

        validateAuthenticationMethod(HttpAuthenticationMethod.BEARER, authenticatedUser);

        String userId = authenticatedUser.getUser().getId();
        String clientId = Preconditions.checkNotNull(authenticatedUser.getOAuthClientId());

        OAuth2WebHookResponse response = new OAuth2WebHookResponse();
        response.setWebHooks(webHookRepository.findByUserIdAndClientId(userId, clientId));

        return response;
    }

    @Override
    public OAuth2WebHook createHook(AuthenticatedUser authenticatedUser,
            se.tink.backend.rpc.webhook.OAuth2WebHook rpcWebhook) {

        validateAuthenticationMethod(HttpAuthenticationMethod.BEARER, authenticatedUser);

        String userId = authenticatedUser.getUser().getId();
        String clientId = Preconditions.checkNotNull(authenticatedUser.getOAuthClientId());

        OAuth2Client client = Preconditions.checkNotNull(oAuth2ClientRepository.findOne(clientId));

        if (!rpcWebhook.validate(client)) {
            httpResponseHelper.error(Status.BAD_REQUEST, "OAuth2WebHook request object not valid");
        }

        OAuth2WebHook coreWebhook = CoreOauth2WebHookMapper.fromMainToCore(rpcWebhook);
        coreWebhook.setId(StringUtils.generateUUID());
        coreWebhook.setClientId(client.getId());
        coreWebhook.setUserId(userId);

        List<OAuth2WebHook> existingWebHooks = webHookRepository.findByUserIdAndClientId(userId, client.getId());

        for (OAuth2WebHook existingWebHook : existingWebHooks) {
            webHookRepository.delete(existingWebHook);
        }

        return webHookRepository.save(coreWebhook);
    }

    @Override
    public void deleteHook(AuthenticatedUser authenticatedUser, String id) {

        validateAuthenticationMethod(HttpAuthenticationMethod.BEARER, authenticatedUser);

        OAuth2WebHook webHook = webHookRepository.findOne(id);

        if (webHook == null) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        if (!Objects.equals(webHook.getUserId(), authenticatedUser.getUser().getId())) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        String clientId = Preconditions.checkNotNull(authenticatedUser.getOAuthClientId());

        if (!Objects.equals(webHook.getClientId(), clientId)) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        webHookRepository.delete(webHook);
    }

    private void validateAuthenticationMethod(HttpAuthenticationMethod requiredMethod,
            AuthenticatedUser authenticatedUser) {
        if (!Objects.equals(authenticatedUser.getMethod(), requiredMethod)) {
            httpResponseHelper.error(Status.UNAUTHORIZED,
                    String.format("Not of %s authentication method", requiredMethod.name()));
        }
    }
}
