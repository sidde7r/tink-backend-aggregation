package se.tink.backend.common.mail.monthly.summary;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Months;
import se.tink.backend.common.mail.SubscriptionHelper;
import se.tink.backend.common.mail.monthly.summary.model.EmailResult;
import se.tink.backend.common.mail.monthly.summary.model.EmptyEmailContent;
import se.tink.backend.common.mail.monthly.summary.renderers.MonthlyEmailNoDataHtmlRendererV2;
import se.tink.backend.common.mail.monthly.summary.utils.PeriodUtils;
import se.tink.backend.common.mail.monthly.summary.utils.UserDeviceUtils;
import se.tink.backend.common.repository.mysql.main.UserDeviceRepository;
import se.tink.backend.common.template.PooledRythmProxy;
import se.tink.backend.core.User;
import se.tink.backend.core.UserDevice;
import se.tink.backend.core.UserProfile;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.i18n.Catalog;

public class MonthlySummaryReminderGenerator {

    private final SubscriptionHelper subscriptionHelper;
    private final UserDeviceRepository userDeviceRepository;
    private final PooledRythmProxy pooledRythmProxy;

    @Inject
    public MonthlySummaryReminderGenerator(SubscriptionHelper subscriptionHelper,
            UserDeviceRepository userDeviceRepository,
            PooledRythmProxy pooledRythmProxy) {
        this.subscriptionHelper = subscriptionHelper;
        this.userDeviceRepository = userDeviceRepository;
        this.pooledRythmProxy = pooledRythmProxy;
    }

    public EmailResult generateEmail(User user) {
        return generateEmail(user, -1, -2);
    }

    public EmailResult generateEmail(User user, int firstMonthOffset, int secondMonthOffset) {

        Preconditions.checkArgument(firstMonthOffset <= 0);
        Preconditions.checkArgument(secondMonthOffset <= 0);
        Preconditions.checkArgument(secondMonthOffset < firstMonthOffset);

        EmailResult email = new EmailResult();

        email.setSubject(getMailSubject(user, firstMonthOffset));
        email.setContent(getMailBody(user, firstMonthOffset));

        return email;
    }

    private String getMailSubject(User user, int monthOffset) {
        PeriodUtils periodUtils = new PeriodUtils(user.getProfile());

        String period = periodUtils.getMonthPeriodFromToday(Months.months(monthOffset));

        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());

        SimpleDateFormat monthFormat = new SimpleDateFormat(catalog.getString("MMMMM yyyy"),
                Catalog.getLocale(user.getProfile().getLocale()));

        return StringUtils.firstLetterUppercaseFormatting(monthFormat.format(DateTime.parse(period).toDate()));
    }

    private String getMailBody(final User user, int firstMonthOffset) {

        MonthlyEmailNoDataHtmlRendererV2 htmlGenerator = new MonthlyEmailNoDataHtmlRendererV2();

        PeriodUtils periodUtils = new PeriodUtils(user.getProfile());

        final String firstMonthPeriod = periodUtils.getMonthPeriodFromToday(Months.months(firstMonthOffset));

        List<UserDevice> userDevices = userDeviceRepository.findByUserId(user.getId());

        int iosCount = UserDeviceUtils.getNumberOfIosDevices(userDevices);
        int androidCount = UserDeviceUtils.getNumberOfAndroidDevices(userDevices);

        EmptyEmailContent emailContent = new EmptyEmailContent();
        emailContent.setLocale(user.getProfile().getLocale());
        emailContent.setUnsubscribeToken(subscriptionHelper.getOrCreateTokenFor(user.getId()));
        emailContent.setUserId(user.getId());
        emailContent.setStartDate(UserProfile.ProfileDateUtils.getFirstDateFromPeriod(firstMonthPeriod, user.getProfile()));
        emailContent.setEndDate(UserProfile.ProfileDateUtils.getLastDateFromPeriod(firstMonthPeriod, user.getProfile()));
        emailContent.setAndroidUser(androidCount > iosCount);

        return htmlGenerator.renderEmail(pooledRythmProxy, emailContent);
    }
}
