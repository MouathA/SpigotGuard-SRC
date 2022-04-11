package org.intellij.lang.annotations;

import java.lang.annotation.*;
import org.jetbrains.annotations.*;

@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.CLASS)
@Language("RegExp")
public @interface RegExp {
    @NonNls
    String suffix() default "";
    
    @NonNls
    String prefix() default "";
}
