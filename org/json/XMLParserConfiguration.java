package org.json;

import java.util.*;

public class XMLParserConfiguration
{
    public static final XMLParserConfiguration ORIGINAL;
    private Map<String, XMLXsiTypeConverter<?>> xsiTypeMap;
    public static final XMLParserConfiguration KEEP_STRINGS;
    private boolean keepStrings;
    private String cDataTagName;
    private boolean convertNilAttributeToNull;
    
    public String getcDataTagName() {
        return this.cDataTagName;
    }
    
    @Deprecated
    public XMLParserConfiguration(final String s) {
        this(false, s, false);
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return this.clone();
    }
    
    public XMLParserConfiguration withConvertNilAttributeToNull(final boolean convertNilAttributeToNull) {
        final XMLParserConfiguration clone = this.clone();
        clone.convertNilAttributeToNull = convertNilAttributeToNull;
        return clone;
    }
    
    @Deprecated
    public XMLParserConfiguration(final boolean b) {
        this(b, "content", false);
    }
    
    public XMLParserConfiguration() {
        this.keepStrings = false;
        this.cDataTagName = "content";
        this.convertNilAttributeToNull = false;
        this.xsiTypeMap = Collections.emptyMap();
    }
    
    public XMLParserConfiguration withXsiTypeMap(final Map<String, XMLXsiTypeConverter<?>> map) {
        final XMLParserConfiguration clone = this.clone();
        clone.xsiTypeMap = Collections.unmodifiableMap((Map<? extends String, ? extends XMLXsiTypeConverter<?>>)new HashMap<String, XMLXsiTypeConverter<?>>(map));
        return clone;
    }
    
    @Deprecated
    public XMLParserConfiguration(final boolean keepStrings, final String cDataTagName, final boolean convertNilAttributeToNull) {
        this.keepStrings = keepStrings;
        this.cDataTagName = cDataTagName;
        this.convertNilAttributeToNull = convertNilAttributeToNull;
    }
    
    public XMLParserConfiguration withcDataTagName(final String cDataTagName) {
        final XMLParserConfiguration clone = this.clone();
        clone.cDataTagName = cDataTagName;
        return clone;
    }
    
    public XMLParserConfiguration withKeepStrings(final boolean keepStrings) {
        final XMLParserConfiguration clone = this.clone();
        clone.keepStrings = keepStrings;
        return clone;
    }
    
    public boolean isKeepStrings() {
        return this.keepStrings;
    }
    
    public boolean isConvertNilAttributeToNull() {
        return this.convertNilAttributeToNull;
    }
    
    private XMLParserConfiguration(final boolean keepStrings, final String cDataTagName, final boolean convertNilAttributeToNull, final Map<String, XMLXsiTypeConverter<?>> map) {
        this.keepStrings = keepStrings;
        this.cDataTagName = cDataTagName;
        this.convertNilAttributeToNull = convertNilAttributeToNull;
        this.xsiTypeMap = Collections.unmodifiableMap((Map<? extends String, ? extends XMLXsiTypeConverter<?>>)map);
    }
    
    public Map<String, XMLXsiTypeConverter<?>> getXsiTypeMap() {
        return this.xsiTypeMap;
    }
    
    @Deprecated
    public XMLParserConfiguration(final boolean keepStrings, final String cDataTagName) {
        this.keepStrings = keepStrings;
        this.cDataTagName = cDataTagName;
        this.convertNilAttributeToNull = false;
    }
    
    static {
        ORIGINAL = new XMLParserConfiguration();
        KEEP_STRINGS = new XMLParserConfiguration().withKeepStrings(true);
    }
    
    @Override
    protected XMLParserConfiguration clone() {
        return new XMLParserConfiguration(this.keepStrings, this.cDataTagName, this.convertNilAttributeToNull, this.xsiTypeMap);
    }
}
