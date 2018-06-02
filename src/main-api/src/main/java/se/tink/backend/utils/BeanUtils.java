package se.tink.backend.utils;

import java.lang.annotation.Annotation;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.util.ReflectionUtils.AnnotationFieldFilter;
import org.springframework.util.ReflectionUtils;
import se.tink.backend.core.Creatable;
import se.tink.backend.core.Modifiable;

public class BeanUtils {
    public static void copyProperties(final Object source, final Object target,
            final Class<? extends Annotation> annotationClass) {
        final BeanWrapper sourceWrapper = new BeanWrapperImpl(source);
        final BeanWrapper targetWrapper = new BeanWrapperImpl(target);

        ReflectionUtils.doWithFields(target.getClass(),
                field -> targetWrapper
                        .setPropertyValue(field.getName(), sourceWrapper.getPropertyValue(field.getName())),
                new AnnotationFieldFilter(annotationClass));
    }

    /**
     * Helper method to copy {@link Creatable} properties from one object to another.
     *
     * @param source object to copy from
     * @param target object to copy to
     */
    public static void copyCreatableProperties(final Object source, final Object target) {
        BeanUtils.copyProperties(source, target, Creatable.class);
    }

    /**
     * Helper method to copy {@link Modifiable} properties from one object to another.
     *
     * @param source object to copy from
     * @param target object to copy to
     */
    public static void copyModifiableProperties(final Object source, final Object target) {
        BeanUtils.copyProperties(source, target, Modifiable.class);
    }
}
