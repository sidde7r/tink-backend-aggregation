package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.TransferEntity;

public class TransferClientParserTest {

    @Test
    public void parseUpcoming_ReturnsCorrectTransfers() throws IOException {
        String fileName = "data/agents/sbab/test/transfers/upcoming-transfers-table.html";
        String htmlResponse = Files.toString(new File(fileName), Charsets.UTF_8);
        Element element = Jsoup.parse(htmlResponse).select("table.kommandeoversikt").first();
        List<TransferEntity> transferToAcceptEntities =
                TransferClientParser.parseUpcomingTransfers(element);

        Assert.assertEquals(transferToAcceptEntities.get(0).getSourceMessage(), "TEST1a");
        Assert.assertEquals(transferToAcceptEntities.get(0).getDestinationMessage(), "TEST1b");
        Assert.assertEquals(transferToAcceptEntities.get(0).getDate(), "2017-03-31");
        Assert.assertEquals(transferToAcceptEntities.get(0).getNegativeAmount(), -1d, 0);

        Assert.assertEquals(transferToAcceptEntities.get(1).getSourceMessage(), "TEST2a");
        Assert.assertEquals(transferToAcceptEntities.get(1).getDestinationMessage(), "TEST2b");
        Assert.assertEquals(transferToAcceptEntities.get(1).getDate(), "2017-03-31");
        Assert.assertEquals(transferToAcceptEntities.get(1).getNegativeAmount(), -2d, 0);
    }
}
