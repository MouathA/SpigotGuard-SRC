package org.jetbrains.annotations;

import java.lang.annotation.*;

public final class ApiStatus
{
    private ApiStatus() {
        throw new AssertionError((Object)"ApiStatus should not be instantiated");
    }
    
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PACKAGE })
    @Documented
    @Retention(RetentionPolicy.CLASS)
    public @interface ScheduledForRemoval {
        String inVersion() default "";
    }
    
    @Retention(RetentionPolicy.CLASS)
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PACKAGE })
    @Documented
    public @interface Internal {
    }
    
    @Retention(RetentionPolicy.CLASS)
    @Documented
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PACKAGE })
    public @interface AvailableSince {
        String value();
    }
    
    @Documented
    @Retention(RetentionPolicy.CLASS)
    @Target({ ElementType.TYPE, ElementType.METHOD })
    public @interface OverrideOnly {
    }
    
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Documented
    @Retention(RetentionPolicy.CLASS)
    public @interface NonExtendable {
    }
    
    @Retention(RetentionPolicy.CLASS)
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PACKAGE })
    @Documented
    public @interface Experimental {
    }
}
