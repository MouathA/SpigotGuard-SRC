package org.jetbrains.annotations;

import java.lang.annotation.*;

@Target({ ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.FIELD, ElementType.TYPE_USE })
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface PropertyKey {
    @NonNls
    String resourceBundle();
}
