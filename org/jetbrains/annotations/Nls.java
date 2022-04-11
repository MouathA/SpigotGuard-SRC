package org.jetbrains.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Documented
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE, ElementType.TYPE, ElementType.PACKAGE })
public @interface Nls {
    Capitalization capitalization() default Capitalization.NotSpecified;
    
    public enum Capitalization
    {
        private static final Capitalization[] $VALUES;
        
        NotSpecified, 
        Sentence, 
        Title;
        
        static {
            $VALUES = new Capitalization[] { Capitalization.NotSpecified, Capitalization.Title, Capitalization.Sentence };
        }
    }
}
