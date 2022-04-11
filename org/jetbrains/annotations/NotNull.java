package org.jetbrains.annotations;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE })
@Retention(RetentionPolicy.CLASS)
public @interface NotNull {
    Class<? extends Exception> exception() default Exception.class;
    
    String value() default "";
}
