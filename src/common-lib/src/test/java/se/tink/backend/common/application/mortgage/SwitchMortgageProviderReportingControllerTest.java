package se.tink.backend.common.application.mortgage;

import java.util.Date;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;
import se.tink.backend.common.dao.ApplicationDAO;
import se.tink.backend.common.repository.cassandra.ApplicationArchiveRepository;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationStatus;
import se.tink.backend.core.application.ApplicationArchiveRow;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.date.DateUtils;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class SwitchMortgageProviderReportingControllerTest {

    private ApplicationDAO applicationDAO;
    private ApplicationArchiveRepository applicationArchiveRepository;

    @Before
    public void setUp() {
        Application a1 = getApplication(DateUtils.parseDate("2017-09-09"));
        Application a2 = getApplication(DateUtils.parseDate("2017-10-01"));
        Application a3 = getApplication(DateUtils.parseDate("2017-10-10"));

        ApplicationArchiveRow a1Archive = getApplicationArchiveRow(a1, "1 000 000", "SEB", "SBAB", "800001");
        ApplicationArchiveRow a2Archive = getApplicationArchiveRow(a2, "2 000 000", "Danske Bank", "SEB", "999");
        ApplicationArchiveRow a3Archive = getApplicationArchiveRow(a3, "3 000 000", "SBAB", "SEB", "1000");

        applicationDAO = mock(ApplicationDAO.class);
        when(applicationDAO.streamAll()).thenReturn(Observable.just(a1, a2, a3));

        applicationArchiveRepository = mock(ApplicationArchiveRepository.class);
        when(applicationArchiveRepository.findByUserIdAndApplicationId(any(), eq(a1.getId()))).thenReturn(
                Optional.ofNullable(a1Archive));
        when(applicationArchiveRepository.findByUserIdAndApplicationId(any(), eq(a2.getId()))).thenReturn(
                Optional.ofNullable(a2Archive));
        when(applicationArchiveRepository.findByUserIdAndApplicationId(any(), eq(a3.getId()))).thenReturn(
                Optional.ofNullable(a3Archive));
    }

    @Test
    public void loadDataAndCompile() {
        SwitchMortgageProviderReportingController controller = new SwitchMortgageProviderReportingController(
                applicationDAO, applicationArchiveRepository, null, null);
        controller.setDryRun(true);
        controller.setPrint(true);

        Date referenceDate = DateUtils.parseDate("2017-11-01");
        CompileAndSendReportCommand command = CompileAndSendReportCommand.forLastCompleteMonth(referenceDate);
        controller.compileAndSendReport(command);
    }

    private ApplicationArchiveRow getApplicationArchiveRow(Application application, String amount, String fromProvider,
            String toProvider, String externalId) {
        ApplicationArchiveRow row = new ApplicationArchiveRow();
        row.setApplicationId(application.getId());
        row.setExternalId(externalId);
        row.setContent(String.format(
                "[{\"title\":\"Bolån\",\"fields\":[{\"title\":\"Befintliga lån\",\"values\":[[\"%s 1,23 %% ränta\",\"%s kr\"],[\"Omfattas inte av amorteringskravet\"]]},{\"title\":\"Nytt lån\",\"values\":[[\"%s Bolån 1,34 %% ränta\",\"%s kr\"]]}],\"populated\":true}]",
                fromProvider, amount, toProvider, amount));
        return row;
    }

    private Application getApplication(Date executionDate) {
        ApplicationStatus status = new ApplicationStatus();
        status.setKey(ApplicationStatusKey.EXECUTED);
        status.setUpdated(executionDate);

        Application application = new Application();
        application.setType(ApplicationType.SWITCH_MORTGAGE_PROVIDER);
        application.setStatus(status);

        return application;
    }
}
