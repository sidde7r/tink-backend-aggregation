package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities.OutputEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class EntityTest {
    @Test
    public void testErrorsEntityNull() {
        String errorsNull = "{ \"challenge\": \"joy\", \"errors\": null, \"myvalue\": \"true\" }";

        OutputEntity outputEntity =
                SerializationUtils.deserializeFromString(errorsNull, OutputEntity.class);

        Assert.assertNull(outputEntity.getErrors());

        System.out.println(SerializationUtils.serializeToString(outputEntity));
    }

    @Test
    public void testErrorsEntityDict() {
        String errorsDict =
                "{ \"errors\": { \"msgCd\": \"MB0027\", \"msgTypeCd\": \"E\" }, \"serverTime\": \"0\" }";

        OutputEntity outputEntity =
                SerializationUtils.deserializeFromString(errorsDict, OutputEntity.class);

        Assert.assertNotNull(outputEntity.getErrors());
        Assert.assertEquals(1, outputEntity.getErrors().size());
        Assert.assertNotNull(outputEntity.getErrors().get(0));
        Assert.assertEquals("MB0027", outputEntity.getErrors().get(0).getMsgCd());
    }

    @Test
    public void testErrorsEntityList() {
        String errorsList =
                "{ \"errors\": [ { \"msgCd\": \"MB0025\" }, { \"msgCd\": \"MB0015\" } ], \"serverTime\": \"0\" }";

        OutputEntity outputEntity =
                SerializationUtils.deserializeFromString(errorsList, OutputEntity.class);

        System.out.println(SerializationUtils.serializeToString(outputEntity));

        Assert.assertNotNull(outputEntity.getErrors());
        Assert.assertEquals(2, outputEntity.getErrors().size());
        Assert.assertNotNull(outputEntity.getErrors().get(0));
        Assert.assertNotNull(outputEntity.getErrors().get(1));
        Assert.assertEquals("MB0025", outputEntity.getErrors().get(0).getMsgCd());
        Assert.assertEquals("MB0015", outputEntity.getErrors().get(1).getMsgCd());
    }
}
