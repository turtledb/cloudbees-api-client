package com.cloudbees.api.config;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;

import java.lang.Override;import java.lang.String;import java.util.AbstractSet;import java.util.ArrayList;import java.util.Iterator;import java.util.List;import java.util.Map;import java.util.Set;

/**
 * {@code Map<String,String>} that's actually stored as
 * List of {@link ParameterSettings}.
 *
 * In this way, we can get the desired XML format and the desired
 * user view.
 *
 * @param <T>
 *     This pointless parameterization is to work around a bug in XStream.
 *     Unless the collection type is explicitly parameterized, it will fail
 *     to compute the correct item type.
 *
 * @author Kohsuke Kawaguchi
 */
@XStreamConverter(CollectionConverter.class)
class ParameterList<T extends ParameterSettings> extends ArrayList<ParameterSettings> {
    private final List<ParameterSettings> listView = this;

    /**
     * Map view over a list of {@link ParameterSettings}.
     */
    private final ParameterMap mapView = new ParameterMap() {
        @Override
        public Set<Entry<String, String>> entrySet() {
            return setView;
        }

        @Override
        public String put(String key, String value) {
            for (ParameterSettings p : listView) {
                if (p.getName().equals(key)) {
                    return p.setValue(value);
                }
            }
            listView.add(new ParameterSettings(key,value));
            return null;
        }
    };

    /**
     * Set view
     */
    private final AbstractSet<Map.Entry<String,String>> setView = new AbstractSet<Map.Entry<String, String>>() {
        @Override
        public Iterator<Map.Entry<String,String>> iterator() {
            return (Iterator) listView.iterator();
        }

        @Override
        public int size() {
            return listView.size();
        }
    };

    public ParameterMap asMap() {
        return mapView;
    }
}
