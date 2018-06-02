package se.tink.backend.insights.core.domain.model;

import java.util.Date;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.insights.core.valueobjects.Amount;
import se.tink.backend.insights.core.valueobjects.EInvoiceId;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.libraries.date.DateUtils;

public class EInvoiceInsightTest {

    @Test
    public void testCalculateInsightScore30DaysUntilDueDate() {
        Date in30days = DateUtils.addDays(new Date(), 30);
        EinvoiceInsight einvoiceInsight = getEInvoiceInsight(in30days);
        Assert.assertTrue(einvoiceInsight.calculateInsightScore() == 10);
    }

    @Test
    public void testCalculateInsightScorePastDueDate() {
        Date yesterday = DateUtils.addDays(new Date(), -1);
        EinvoiceInsight einvoiceInsight = getEInvoiceInsight(yesterday);
        Assert.assertTrue(einvoiceInsight.calculateInsightScore() == 100);
    }

    @Test
    public void testCalculateInsightScore15DaysTillDueDate() {
        Date in15Days = DateUtils.addDays(new Date(), 15);
        EinvoiceInsight einvoiceInsight = getEInvoiceInsight(in15Days);
        Assert.assertTrue(einvoiceInsight.calculateInsightScore() == 50);
    }

    private EinvoiceInsight getEInvoiceInsight(Date date) {
        return new EinvoiceInsight(UserId.of("asdf"), EInvoiceId.of(UUID.randomUUID().toString()), Amount.of(500), date,
                "message");
    }
}
