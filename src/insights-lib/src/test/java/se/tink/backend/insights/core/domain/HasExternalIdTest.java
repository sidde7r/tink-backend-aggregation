package se.tink.backend.insights.core.domain;

import com.google.common.base.Strings;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.insights.core.domain.model.EinvoiceInsight;
import se.tink.backend.insights.core.domain.model.EinvoiceOverdueInsight;
import se.tink.backend.insights.core.domain.model.HasExternalId;
import se.tink.backend.insights.core.domain.model.Insight;
import se.tink.backend.insights.core.domain.model.LeftToSpendLowInsight;
import se.tink.backend.insights.core.valueobjects.Amount;
import se.tink.backend.insights.core.valueobjects.EInvoiceId;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.libraries.date.DateUtils;

public class HasExternalIdTest {

    @Test
    public void setUp() throws Exception {
        Date today = DateUtils.getToday();
        List<Insight> insights = Lists.newArrayList(
                getLeftToSpendLowInsight(),
                getEInvoiceInsight(today),
                getEinvoiceOverdueInsight(today));

        insights.forEach(insight -> {
            if(insight instanceof HasExternalId) {
                String id = ((HasExternalId) insight).getExternalId();
                Assert.assertFalse(Strings.isNullOrEmpty(id));
            }
        });
    }

    private LeftToSpendLowInsight getLeftToSpendLowInsight(){
        return new LeftToSpendLowInsight(UserId.of("asdf"), Amount.of(400));
    }

    private EinvoiceOverdueInsight getEinvoiceOverdueInsight(Date date){
        return new EinvoiceOverdueInsight(UserId.of("asdf"), EInvoiceId.of(UUID.randomUUID().toString()), Amount.of(500), date,
                "message");
    }

    private EinvoiceInsight getEInvoiceInsight(Date date) {
        return new EinvoiceInsight(UserId.of("asdf"), EInvoiceId.of(UUID.randomUUID().toString()), Amount.of(500), date,
                "message");
    }
}
