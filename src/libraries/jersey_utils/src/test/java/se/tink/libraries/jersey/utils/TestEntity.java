package se.tink.libraries.jersey.utils;

import org.junit.Ignore;

@Ignore
public class TestEntity implements SafelyLoggable {

    String someString = "some vaule";
    int someInt = 123;

    @Override
    public String toSafeString() {
        return toString();
    }

    @Override
    public String toString() {
        return "$classname{" + "someString='" + someString + '\'' + ", someInt=" + someInt + '}';
    }
}
