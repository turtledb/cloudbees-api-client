package com.cloudbees.api.config;

import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.lang.Exception;
import java.lang.Object;
import java.lang.String;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Kohsuke Kawaguchi
 */
public class ConfigParametersTest {
    /**
     * Tests the basic roundtrip conversion.
     */
    @Test
    public void testUnmarshal() throws Exception {
        String xml = IOUtils.toString(getClass().getResourceAsStream("config1.xml"));
        ConfigParameters c = ConfigParameters.parse(xml);
        assertThat(c.toXML(), is(xml));
    }

    @Test
    public void testInt() throws Exception {
        ConfigParameters c = new ConfigParameters();
        ParameterMap r = c.getRuntimeParameters();
        r.putInt("a", 8);
        assertThat(r.getInt("a", 0), is(8));
        assertThat(r.getInt("b",999),is(999));
        assertThat(c.toXML(), isXML("<config><runtime-param name='a' value='8'/></config>"));
    }

    /**
     * Whitespace and quotation insensitive matcher.
     */
    private static Matcher<String> isXML(final String xml) {
        return new BaseMatcher<String>() {
            
        	public boolean matches(Object item) {
    			return compress(xml).equals(compress(item.toString()));
            }
        	
            public void describeTo(Description description) {
    			description.appendValue(xml);
            }

            private String compress(String xml) {
                String unindented = Pattern.compile(">[^<]+<").matcher(xml).replaceAll("><");
                return unindented.replace('"','\'');    // normalize quote mark
            }

        };
    }

}
