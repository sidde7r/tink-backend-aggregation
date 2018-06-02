package se.tink.backend.system.controllers;

import java.util.Date;
import org.junit.Test;
import se.tink.backend.common.config.BackOfficeConfiguration;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.core.ApplicationStatus;
import se.tink.backend.core.enums.ApplicationStatusKey;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class BackOfficeNotificationControllerTest {
    @Test
    public void sendEmailIfApplicationIsRejected() throws Exception {
        MailSender mailSender = mockSuccessfulMailSender();
        BackOfficeConfiguration configuration = mock(BackOfficeConfiguration.class);

        final String externalApplicationId = "123456";
        final ApplicationStatus status = new ApplicationStatus();
        status.setKey(ApplicationStatusKey.REJECTED);
        status.setUpdated(new Date());

        assertTrue(new BackOfficeNotificationController(mailSender, configuration)
                .notifyBackOfficeAboutApplicationStatus(externalApplicationId, status));

        verify(mailSender, times(1))
                .sendMessage(any(), eq("CANCELLED: 123456"), any(), any(), contains("REJECTED"), eq(true));
        verifyNoMoreInteractions(mailSender);
    }

    private MailSender mockSuccessfulMailSender() {
        MailSender mailSender = mock(MailSender.class);
        when(mailSender.sendMessage(any(), any(), any(), any(), any(), eq(true))).thenReturn(true);
        return mailSender;
    }
}
