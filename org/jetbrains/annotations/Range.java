package org.jetbrains.annotations;

import java.lang.annotation.*;

@Target({ ElementType.TYPE_USE })
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface Range {
    long from();
    
    long to();
}
