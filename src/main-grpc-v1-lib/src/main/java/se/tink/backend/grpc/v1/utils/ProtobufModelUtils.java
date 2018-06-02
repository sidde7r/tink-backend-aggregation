package se.tink.backend.grpc.v1.utils;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ProtobufModelUtils {

    public static Timestamp toProtobufTimestamp(Date date) {
        return toProtobufTimestamp(date.getTime());
    }

    public static Timestamp getCurrentProtobufTimestamp() {
        return toProtobufTimestamp(Instant.now());
    }

    public static Timestamp toProtobufTimestamp(Instant instant) {
        return Timestamp.newBuilder().setSeconds(instant.getEpochSecond()).setNanos(instant.getNano()).build();
    }

    public static Timestamp toProtobufTimestamp(long timeMs) {
        return Timestamp.newBuilder().setSeconds(timeMs / 1000).setNanos((int) ((timeMs % 1000) * 1000000)).build();
    }

    public static Date timestampToDate(Timestamp timestamp) {
        return new Date(TimeUnit.SECONDS.toMillis(timestamp.getSeconds()) + TimeUnit.NANOSECONDS
                .toMillis(timestamp.getNanos()));
    }

    public static BoolValue toBoolValue(boolean bool) {
        return BoolValue.newBuilder().setValue(bool).build();
    }

    public static Int32Value toInt32Value(int value) {
        return Int32Value.newBuilder().setValue(value).build();
    }

    public static StringValue toStringValue(String string) {
        return StringValue.newBuilder().setValue(string).build();
    }

}
