package org.jetbrains.annotations;

import java.lang.annotation.*;
import org.intellij.lang.annotations.*;

public final class Debug
{
    private Debug() {
        throw new AssertionError((Object)"Debug should not be instantiated");
    }
    
    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.CLASS)
    public @interface Renderer {
        @Language(value = "JAVA", prefix = "class Renderer{Object[] $childrenArray(){return ", suffix = ";}}")
        @NonNls
        String childrenArray() default "";
        
        @NonNls
        @Language(value = "JAVA", prefix = "class Renderer{String $text(){return ", suffix = ";}}")
        String text() default "";
        
        @NonNls
        @Language(value = "JAVA", prefix = "class Renderer{boolean $hasChildren(){return ", suffix = ";}}")
        String hasChildren() default "";
    }
}
