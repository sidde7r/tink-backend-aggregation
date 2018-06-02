package se.tink.backend.common.workers.fraud.processors;

import com.google.common.collect.Lists;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.common.workers.fraud.FraudDataProcessorContext;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.User;

public class FraudDataDeduplicationProcessorTest {
    @Test
    public void testDontDeduplicateMovingBackToPreviousAddress() throws Exception {

        FraudDataProcessorContext mockedContext = new FraudDataProcessorContext();
        mockedContext.setUser(new User());

        List<FraudDetails> inStoreAddresses = getAddresses();
        mockedContext.setInStoreFraudDetails(inStoreAddresses);

        FraudDetails addressA = inStoreAddresses.get(0);

        mockedContext.setFraudDetailsContent(Lists.newArrayList(addressA.getContent()));

        FraudDataDeduplicationProcessor processor = new FraudDataDeduplicationProcessor();
        processor.process(mockedContext);

        Assert.assertNotEquals(0, mockedContext.getFraudDetailsContent().size());
        if (!mockedContext.getFraudDetailsContent().isEmpty()) {
            Assert.assertEquals(mockedContext.getFraudDetailsContent().get(0).getContentId(), addressA.getContent().getContentId());
        }
    }

    @Test
    public void testDeduplicateConsecutiveAddresses() throws Exception {

        FraudDataProcessorContext mockedContext = new FraudDataProcessorContext();
        mockedContext.setUser(new User());

        List<FraudDetails> inStoreAddresses = getAddresses();
        mockedContext.setInStoreFraudDetails(inStoreAddresses);

        FraudDetails addressB = inStoreAddresses.get(1);

        mockedContext.setFraudDetailsContent(Lists.newArrayList(addressB.getContent()));

        FraudDataDeduplicationProcessor processor = new FraudDataDeduplicationProcessor();
        processor.process(mockedContext);

        Assert.assertEquals(0, mockedContext.getFraudDetailsContent().size());
    }

    private FraudDetails getAddressDetails(FraudAddressContent content) {
        FraudDetails addressDetails = new FraudDetails();
        addressDetails.setContent(content);
        addressDetails.setType(FraudDetailsContentType.ADDRESS);

        return addressDetails;
    }

    private FraudDetails getAddressA() {
        FraudAddressContent addressAContent = new FraudAddressContent();
        addressAContent.setAddress("Drottninggatan 74");
        addressAContent.setCity("Stockholm");
        addressAContent.setPostalcode("111 21");
        addressAContent.setContentType(FraudDetailsContentType.ADDRESS);

        return getAddressDetails(addressAContent);
    }

    private FraudDetails getAddressB() {
        FraudAddressContent addressBContent = new FraudAddressContent();
        addressBContent.setAddress("Vasagatan 11");
        addressBContent.setCity("Stockholm");
        addressBContent.setPostalcode("111 20");
        addressBContent.setContentType(FraudDetailsContentType.ADDRESS);

        return getAddressDetails(addressBContent);
    }

    private List<FraudDetails> getAddresses() {
        FraudDetails addressA = getAddressA();
        FraudDetails addressB = getAddressB();

        addressA.setDate(DateTime.parse("2014-01-01").toDate());
        addressB.setDate(DateTime.parse("2015-01-01").toDate());

        return Lists.newArrayList(addressA, addressB);
    }
}
