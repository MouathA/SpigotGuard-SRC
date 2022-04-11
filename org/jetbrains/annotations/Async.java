package org.jetbrains.annotations;

import java.lang.annotation.*;

public final class Async
{
    private Async() {
        throw new AssertionError((Object)"Async should not be instantiated");
    }
    
    @Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER })
    @Retention(RetentionPolicy.CLASS)
    public @interface Schedule {
    }
    
    @Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER })
    @Retention(RetentionPolicy.CLASS)
    public @interface Execute {
    }
}
