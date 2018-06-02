package se.tink.backend.serialization.protobuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.tink.backend.core.UserContext;
import se.tink.libraries.serialization.utils.SerializationUtils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class SerializationTest {

    static UserContext context;
    static String benchmarkContext;
    ProtobufMessageBodyReader<UserContext> reader;
    ProtobufMessageBodyWriter writer;

    ByteArrayOutputStream output;
    ByteArrayInputStream input;

    @BeforeClass
    public static void setUpClass() throws IOException {
        String raw = Files.toString(new File("data/test/usercontext.json"), Charsets.UTF_8);
        context = SerializationUtils.deserializeFromString(raw, UserContext.class);

        ProtobufMessageBodyReader<UserContext> reader = new ProtobufMessageBodyReader<>();
        ProtobufMessageBodyWriter writer = new ProtobufMessageBodyWriter();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        writer.writeTo(context, UserContext.class, null, null, null, null, output);
        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        UserContext context = reader.readFrom(UserContext.class, null, null, null, null, input);

        benchmarkContext = SerializationUtils.serializeToString(context);
    }

    @Before
    public void setUp() throws IOException {
        reader = new ProtobufMessageBodyReader<>();
        writer = new ProtobufMessageBodyWriter();
        output = new ByteArrayOutputStream();
    }

    /**
     * This test tests JSON(deproto(proto(UserContext))) == JSON(deproto(proto(UserContext)))
     * 
     * @throws IOException
     */
    @Test
    public void test() throws IOException {
        writer.writeTo(context, UserContext.class, null, null, null, null, output);
        input = new ByteArrayInputStream(output.toByteArray());
        UserContext context = reader.readFrom(UserContext.class, null, null, null, null, input);

        String actualContext = SerializationUtils.serializeToString(context);
        Assert.assertEquals(benchmarkContext, actualContext);
    }
}
