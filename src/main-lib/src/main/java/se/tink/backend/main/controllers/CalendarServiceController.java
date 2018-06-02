package se.tink.backend.main.controllers;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.joda.time.LocalDate;
import se.tink.backend.common.concurrency.StatisticsActivitiesLock;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.core.UserState;
import se.tink.backend.main.providers.calendar.BusinessDayListProvider;
import se.tink.backend.main.providers.calendar.PeriodListProvider;
import se.tink.backend.main.rpc.calendar.GetBusinessDaysCommand;
import se.tink.backend.main.rpc.calendar.GetPeriodListCommand;
import se.tink.backend.main.validators.PeriodListValidator;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.date.ThreadSafeDateFormat;

import com.google.inject.Inject;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CalendarServiceController {
    private final BusinessDayListProvider businessDayListProvider;
    private final UserStateRepository userStateRepository;
    private final CuratorFramework coordinationClient;
    private final PeriodListProvider periodListProvider;

    static final String YEAR = "^\\d{4}";
    static final String YEAR_MONTH_SINGLE = "^\\d{4}-\\d{1}$";
    static final String YEAR_MONTH_DOOUBLE = "^\\d{4}-\\d{2}$";
    static final String DAY = "^\\d{4}-\\d{2}-\\d{2}$";
    private static final int USER_STATE_WAIT_FOR_READ_TIME_SEC = 10;
    private static final LogUtils log = new LogUtils(CalendarServiceController.class);


    @Inject
    public CalendarServiceController(BusinessDayListProvider businessDayListProvider, UserStateRepository userStateRepository, CuratorFramework coordinationClient, PeriodListProvider periodListProvider) {
        this.businessDayListProvider = businessDayListProvider;
        this.userStateRepository = userStateRepository;
        this.coordinationClient = coordinationClient;
        this.periodListProvider = periodListProvider;
    }

    public  Map<String, Map<String, List<Integer>>> getBusinessDays(GetBusinessDaysCommand command) {
        return businessDayListProvider.listBusinessDays(new LocalDate(command.getStartYear(), command.getStartMonth(), 1), command.getMonths());
    }

    public List<Period> list(GetPeriodListCommand command) {
        List<String> monthsToGetPeriodFor = Lists.newArrayList();

        List<Period> listPeriods = Lists.newArrayList();

        final String period = command.getPeriod();
        final ResolutionTypes periodMode = command.getPeriodMode();
        final int periodAdjustedDay = command.getPeriodAdjustedDay();
        // DAY
        if (period.matches(DAY)) {
            try {
                monthsToGetPeriodFor.addAll(Lists.newArrayList(DateUtils.getMonthPeriod(
                        ThreadSafeDateFormat.FORMATTER_DAILY.parse(period), periodMode,
                        periodAdjustedDay)));
            } catch (ParseException e) {
                log.error(command.getUserId(), "Error parsing period: " + period, e);
            }

            // YEAR
        } else if (period.matches(YEAR)) {
            int year = Integer.parseInt(period);
            PeriodListValidator.validateStartYear(year);

            monthsToGetPeriodFor.addAll(DateUtils
                    .createPeriodListForYear(year, periodMode, periodAdjustedDay));

            // YEAR-MONTH
        } else if (period.matches(YEAR_MONTH_SINGLE) || period.matches(YEAR_MONTH_DOOUBLE)) {
            String[] splits = period.split("-");
            int year = Integer.parseInt(splits[0]);
            PeriodListValidator.validateStartYear(year);

            if (splits.length > 1 && splits[1] != null) {
                int month = Integer.parseInt(splits[1]);
                PeriodListValidator.validateStartMonth(month);

                monthsToGetPeriodFor.add(DateUtils.createPeriod(year, month));
            }
        }

        if (!monthsToGetPeriodFor.isEmpty()) {
            List<Period> userStatePeriods = getPeriodsFromUserState(command.getUserId());
            listPeriods = periodListProvider
                    .buildListOfPeriods(userStatePeriods, monthsToGetPeriodFor, periodMode,
                            periodAdjustedDay);
        }

        if (listPeriods == null || listPeriods.isEmpty()) {
            throw new IllegalArgumentException("Period not valid: " + period);
        }
        return listPeriods;
    }

    private List<Period> getPeriodsFromUserState(String userId) {
        waitForFreshUserState(userId);
        UserState userState = userStateRepository.findOneByUserId(userId);
        if (userState == null) {
            return Lists.newArrayList();
        }
        return userState.getPeriods();
    }

    private void waitForFreshUserState(String userId) {
        StatisticsActivitiesLock lock = new StatisticsActivitiesLock(coordinationClient, userId);

        try {
            if (!lock.waitForRead(USER_STATE_WAIT_FOR_READ_TIME_SEC, TimeUnit.SECONDS)) {
                log.info(userId, "Timeout while waiting for generating statistics");
            }
        } catch (Exception e) {
            log.error(userId, "Could not wait for fresh statistics", e);
        }
    }


}
