package org.jetbrains.annotations;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.CLASS)
public @interface TestOnly {
}
