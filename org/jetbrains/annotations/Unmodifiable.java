package org.jetbrains.annotations;

import java.lang.annotation.*;

@Target({ ElementType.TYPE_USE })
@Documented
@Retention(RetentionPolicy.CLASS)
public @interface Unmodifiable {
}
