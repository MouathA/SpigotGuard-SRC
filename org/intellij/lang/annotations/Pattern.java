package org.intellij.lang.annotations;

import java.lang.annotation.*;
import org.jetbrains.annotations.*;

@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.ANNOTATION_TYPE })
public @interface Pattern {
    @Language("RegExp")
    @NonNls
    String value();
}
