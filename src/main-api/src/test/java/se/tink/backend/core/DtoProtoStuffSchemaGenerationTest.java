package se.tink.backend.core;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.protostuff.runtime.RuntimeSchema;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.google.common.collect.Lists;

public class DtoProtoStuffSchemaGenerationTest {

    static List<Class<?>> protobufableDtos;

    public static class DuplicatedTagSequenceNumber {
        @Tag(1)
        int a = 5;
        @Tag(1)
        String b = "hej";
        @Tag(2)
        boolean c = false;
    }

    public static class MissingTag {
        int a = 5;
        @Tag(1)
        String b = "hej";
        @Tag(2)
        boolean c = false;
        @Exclude
        String d = "qwe";
    }

    private static boolean hasAnyFieldAnnotatedWith(Class<?> clazz, List<Class<? extends Annotation>> annotations) {
        boolean hasAny = false;

        for (Field field : clazz.getDeclaredFields()) {
            for (Class<? extends Annotation> annotation : annotations) {
                Annotation a = field.getAnnotation(annotation);
                if (a != null) {
                    hasAny = true;
                    break;
                }
            }
        }

        return hasAny;
    }

    @BeforeClass
    public static void setUp() throws NoSuchFieldException {
        protobufableDtos = new ArrayList<>();

        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false), new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(
                        new FilterBuilder().include(FilterBuilder.prefix("se.tink.backend.core")).include(
                                FilterBuilder.prefix("se.tink.backend.rpc"))));

        Set<Class<? extends Object>> allClasses = reflections.getSubTypesOf(Object.class);

        System.out.println("allClasses.size() = " + allClasses.size());

        for (Class<? extends Object> clazz : allClasses) {
            if (clazz.equals(DuplicatedTagSequenceNumber.class) || clazz.equals(MissingTag.class)) {
                continue;
            }
            if (hasAnyFieldAnnotatedWith(clazz, Lists.newArrayList(Tag.class, Exclude.class))) {
                protobufableDtos.add(clazz);
            }
        }
    }

    /**
     * If this start to fail, i.e. Duplicate tag sequence numbers are allowed, ProtoStuff has made changes. Might be
     * worth checking up.
     */
    @Test(expected = IllegalStateException.class)
    public void generateDuplicatedTagSequenceNumberSchema() {
        RuntimeSchema.getSchema(DuplicatedTagSequenceNumber.class);
    }

    /**
     * If this start to fail, i.e. Missing Tags are allowed, then ProtoStuff has made changes. Might be worth checking
     * up.
     */
    @Test(expected = RuntimeException.class)
    public void generateMissingTagSchema() {
        RuntimeSchema.getSchema(MissingTag.class);
    }

    @Test
    public void verifyThereAreSomeProtobufableDtos() {
        assertNotEquals("Something went wrong loading the classes", 0, protobufableDtos.size());
    }

    @Test
    public void createRuntimeSchemaAndDontCrash() {
        System.out.println("Testing:");
        for (Class<?> clazz : protobufableDtos) {
            System.out.println("\t" + clazz.getSimpleName());
            try {
                RuntimeSchema.getSchema(clazz);
            } catch (Exception e) {
                fail("Couldn't generate runtime schema of: " + clazz.getSimpleName() + ".\nError message: "
                        + e.getMessage());
            }
        }
    }
}
