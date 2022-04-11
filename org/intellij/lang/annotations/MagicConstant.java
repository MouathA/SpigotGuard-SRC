package org.intellij.lang.annotations;

import java.lang.annotation.*;
import org.jetbrains.annotations.*;

@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.ANNOTATION_TYPE, ElementType.METHOD })
public @interface MagicConstant {
    long[] flags() default {};
    
    @NonNls
    String[] stringValues() default {};
    
    Class<?> flagsFromClass() default void.class;
    
    long[] intValues() default {};
    
    Class<?> valuesFromClass() default void.class;
}
