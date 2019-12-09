package se.tink.sa.common.mapper;

import java.util.function.BiConsumer;

public class ProtoObjSetter {

    public static <O, V> void setValue(BiConsumer<O, V> setter, O obj, V value) {
        if (value != null) {
            setter.accept(obj, value);
        }
    }
}
