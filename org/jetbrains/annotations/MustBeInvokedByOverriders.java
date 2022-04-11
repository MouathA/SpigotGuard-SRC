package org.jetbrains.annotations;

import java.lang.annotation.*;

@Target({ ElementType.METHOD })
@Documented
@Retention(RetentionPolicy.CLASS)
public @interface MustBeInvokedByOverriders {
}
