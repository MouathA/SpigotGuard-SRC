package org.intellij.lang.annotations;

import java.lang.annotation.*;

@Deprecated
public final class JdkConstants
{
    private JdkConstants() {
        throw new AssertionError((Object)"JdkConstants should not be instantiated");
    }
    
    public @interface CalendarMonth {
    }
    
    public @interface CursorType {
    }
    
    public @interface InputEventMask {
    }
    
    public @interface VerticalScrollBarPolicy {
    }
    
    public @interface PatternFlags {
    }
    
    public @interface FontStyle {
    }
    
    public @interface TreeSelectionMode {
    }
    
    public @interface ListSelectionMode {
    }
    
    public @interface TitledBorderTitlePosition {
    }
    
    public @interface TitledBorderJustification {
    }
    
    public @interface FlowLayoutAlignment {
    }
    
    public @interface HorizontalAlignment {
    }
    
    public @interface BoxLayoutAxis {
    }
    
    public @interface TabPlacement {
    }
    
    public @interface HorizontalScrollBarPolicy {
    }
    
    public @interface TabLayoutPolicy {
    }
    
    public @interface AdjustableOrientation {
    }
}
