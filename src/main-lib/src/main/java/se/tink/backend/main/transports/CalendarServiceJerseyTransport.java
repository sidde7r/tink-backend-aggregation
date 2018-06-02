package se.tink.backend.main.transports;

import com.google.inject.Inject;
import org.assertj.core.util.Lists;
import se.tink.backend.api.CalendarService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.main.controllers.CalendarServiceController;
import se.tink.backend.main.rpc.calendar.GetBusinessDaysCommand;
import se.tink.backend.main.rpc.calendar.GetPeriodListCommand;
import se.tink.backend.rpc.calendar.BusinessDaysResponse;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.Period;
import se.tink.libraries.http.utils.HttpResponseHelper;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

public class CalendarServiceJerseyTransport implements CalendarService {

    private final CalendarServiceController calendarServiceController;
    private final HttpResponseHelper httpResponseHelper;
    private static final LogUtils log = new LogUtils(CalendarServiceJerseyTransport.class);


    @Inject
    public CalendarServiceJerseyTransport(CalendarServiceController calendarServiceController) {
        this.calendarServiceController = calendarServiceController;
        this.httpResponseHelper = new HttpResponseHelper(log);
    }

    @Override
    public BusinessDaysResponse businessDays(AuthenticatedUser user, Integer startYear, Integer startMonth, @Nullable Integer months) {
        Map<String, Map<String, List<Integer>>> businessDays = null;
        try {
            businessDays = calendarServiceController
                    .getBusinessDays(new GetBusinessDaysCommand(startYear, startMonth, months));
        } catch (IllegalArgumentException e) {
            httpResponseHelper.error(Response.Status.BAD_REQUEST, e.getMessage());
        }
        return new BusinessDaysResponse(businessDays);
    }

    @Override
    public List<Period> listPeriods(AuthenticatedUser authenticatedUser, String period) {
        User user = authenticatedUser.getUser();
        UserProfile userProfile = user.getProfile();
        List<Period> periods = Lists.newArrayList();
        try {
            periods = calendarServiceController
                    .list(new GetPeriodListCommand(user.getId(), period, userProfile.getPeriodMode(), userProfile.getPeriodAdjustedDay() ));
        } catch (IllegalArgumentException e) {
            httpResponseHelper.error(Response.Status.BAD_REQUEST, e.getMessage());
        }
        return periods;
    }
}
