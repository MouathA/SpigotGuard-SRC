package org.intellij.lang.annotations;

class PrintFormatPattern
{
    @Language("RegExp")
    private static final String CONVERSION;
    @Language("RegExp")
    private static final String ARG_INDEX;
    @Language("RegExp")
    private static final String WIDTH;
    @Language("RegExp")
    private static final String TEXT;
    @Language("RegExp")
    private static final String PRECISION;
    @Language("RegExp")
    static final String PRINT_FORMAT;
    @Language("RegExp")
    private static final String FLAGS;
    
    static {
        WIDTH = "(?:\\d+)?";
        TEXT = "[^%]|%%";
        FLAGS = "(?:[-#+ 0,(<]*)?";
        CONVERSION = "(?:[tT])?(?:[a-zA-Z%])";
        PRECISION = "(?:\\.\\d+)?";
        ARG_INDEX = "(?:\\d+\\$)?";
        PRINT_FORMAT = "(?:[^%]|%%|(?:%(?:\\d+\\$)?(?:[-#+ 0,(<]*)?(?:\\d+)?(?:\\.\\d+)?(?:[tT])?(?:[a-zA-Z%])))*";
    }
}
