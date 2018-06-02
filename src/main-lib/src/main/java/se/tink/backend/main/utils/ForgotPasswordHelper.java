package se.tink.backend.main.utils;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.Response;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.common.mail.MailTemplate;
import se.tink.backend.common.repository.mysql.main.UserForgotPasswordTokenRepository;
import se.tink.backend.common.utils.QuotationMarker;
import se.tink.backend.core.User;
import se.tink.backend.core.UserForgotPasswordToken;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.i18n.Catalog;

public class ForgotPasswordHelper {

    private static final LogUtils log = new LogUtils(ForgotPasswordHelper.class);

    private final MailSender mailSender;
    private final UserForgotPasswordTokenRepository userForgotPasswordTokenRepository;

    @Inject
    public ForgotPasswordHelper(MailSender mailSender,
            UserForgotPasswordTokenRepository userForgotPasswordTokenRepository) {

        this.mailSender = mailSender;
        this.userForgotPasswordTokenRepository = userForgotPasswordTokenRepository;
    }

    /**
     * Create a forgot password token and send the email to the user.
     */
    public void forgotPassword(User user, MailTemplate template, Optional<String> remoteAddress, Optional<String> userAgent) {
        log.debug(user.getId(), String.format(
                "Asked to generate forgot-password token by remote address %s with user agent %s.", remoteAddress
                        .map(new QuotationMarker()::apply).orElse("<missing>"),
                userAgent.map(new QuotationMarker()::apply).orElse("<missing>")));

        userForgotPasswordTokenRepository.deleteByUserId(user.getId());

        UserForgotPasswordToken token = new UserForgotPasswordToken();

        token.setUserId(user.getId());
        token.setInserted(new Date());

        userForgotPasswordTokenRepository.save(token);

        Map<String, String> parameters = Maps.newHashMap();

        String urlFormat = "https://app.tink.se/password-reset/%s?locale=%s";

        parameters.put("RESET_PASSWORD_URL", String.format(urlFormat, token.getId(), Catalog
                .getLocale(user.getProfile().getLocale()).getLanguage()));

        try {
            mailSender.sendMessageWithTemplate(user, template, parameters);
        } catch (Exception e) {
            log.error(user.getId(), "Could not send forgotten password message", e);
            HttpResponseHelper.error(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
