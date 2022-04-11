package org.jetbrains.annotations;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface Contract {
    @NonNls
    String mutates() default "";
    
    @NonNls
    String value() default "";
    
    boolean pure() default false;
}
