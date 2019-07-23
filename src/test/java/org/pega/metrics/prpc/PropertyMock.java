package org.pega.metrics.prpc;

import com.google.common.collect.ImmutableMap;
import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.pega.metrics.prpc.cache.PrpcTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PropertyMock {

    /*
    top [ClipboardProperty]
        pxResults(1)
            Tag(tag1) = tagVal1
            Tag(tag2) = tagVal2
            Value(val1) = val1
            Value(val2) = val2
        pxResults(2)
            Tag(tag1) = tagVal3
            Tag(tag2) = tagVal4
            Value(val1) = val3
            Value(val2) = val4
     */

    // mockSourceProp.list("pxResults")
    //      .item(tags1, [valName1, val1], [valName2, val2])
    //      .item(tags2, [valName1, val3], [valName2, val4])
    //      .item(tags3, [valName1, val5], [valName2, val6])


    public static MockSourcePropBuilder mockSourcePropBuilder() {
        return new MockSourcePropBuilder();
    }

    public static class MockSourcePropBuilder {

        private List<Pair<TagPropDef, ValuePropDef>> items = new ArrayList<>();
        private boolean isGroup;
        private boolean isList;
        private boolean isPage;

        public MockSourcePropBuilder addItem(TagPropDef tagPropDef, ValuePropDef valuePropDef) {
            items.add(ImmutablePair.of(tagPropDef, valuePropDef));
            return this;
        }

        public MockSourcePropBuilder group() {
            isGroup = true;
            return this;
        }

        public MockSourcePropBuilder list() {
            isList = true;
            return this;
        }

        public MockSourcePropBuilder page() {
            isPage = true;
            return this;
        }

        public ClipboardProperty build() {
            ClipboardProperty ret = mock(ClipboardProperty.class);
            when(ret.size()).thenReturn(items.size());
            if (items.size() == 0) when(ret.isEmpty()).thenReturn(true);
            if (isGroup) when(ret.isGroup()).thenReturn(true);
            if (isList) when(ret.isList()).thenReturn(true);
            if (isPage) when(ret.isPage()).thenReturn(true);

            List<ClipboardProperty> propItems = new ArrayList<>();
            for (Pair<TagPropDef, ValuePropDef> item : items) {
                ClipboardPage itemPage = mockSourceItem(item.getLeft(), item.getRight());
                when(ret.getPageValue(PrpcTags.id(item.getLeft().tags))).then(mock -> itemPage);

                ClipboardProperty itemProp = mock(ClipboardProperty.class);
                when(itemProp.getPageValue()).thenReturn(itemPage);
                when(itemProp.getProperty(item.getLeft().ref)).then(mock -> itemPage.getProperty(item.getLeft().ref));
                propItems.add(itemProp);
            }
            when(ret.iterator()).then(mock -> propItems.iterator());

            return ret;
        }
    }


    public static class TagPropDef {
        private String ref;
        private Tags tags;

        public TagPropDef(String ref, Tags tags) {
            this.ref = ref;
            this.tags = tags;
        }

        public static TagPropDef tagPropDef(String ref, Tags tags) {
            return new TagPropDef(ref, tags);
        }
    }

    public static class ValuePropDef {
        private String ref;
        private Map<String, Double> values;

        public ValuePropDef(String ref, Map values) {
            this.ref = ref;
            this.values = values;
        }

        public static ValuePropDef valuePropDef(String ref, Map<String, Double> values) {
            return new ValuePropDef(ref, values);
        }
    }

    public static ClipboardPage mockSourceItem(TagPropDef tagPropDef, ValuePropDef valuePropDef) {
        ClipboardPage ret = mock(ClipboardPage.class);

        ClipboardProperty tagProp = mockTagProp(tagPropDef);
        when(ret.getProperty(eq(tagPropDef.ref))).thenReturn(tagProp);
        when(ret.getIfPresent(eq(tagPropDef.ref))).thenReturn(tagProp);

        ClipboardProperty valueProp = mockValueProp(valuePropDef);
        when(ret.getProperty(eq(valuePropDef.ref))).thenReturn(valueProp);
        when(ret.getIfPresent(eq(valuePropDef.ref))).thenReturn(valueProp);

        for (Map.Entry<String, Double> entry : valuePropDef.values.entrySet()) {
            when(ret.getProperty(eq(valuePropDef.ref + "(" + entry.getKey() + ")"))).then(mock -> valueProp.getPropertyValue(entry.getKey()));
            when(ret.getIfPresent(eq(valuePropDef.ref + "(" + entry.getKey() + ")"))).then(mock -> valueProp.getPropertyValue(entry.getKey()));
        }

        return ret;
    }

    public static ClipboardProperty mockTagProp(TagPropDef tagPropDef) {
        ClipboardProperty ret = mock(ClipboardProperty.class);

        List<ClipboardProperty> list = new ArrayList<>();
        for (Tag tag : tagPropDef.tags) {
            ClipboardProperty tagProp = mock(ClipboardProperty.class);
            when(tagProp.getName()).thenReturn(tag.getKey());
            when(tagProp.getStringValue()).thenReturn(tag.getValue());
            list.add(tagProp);
        }

        when(ret.iterator()).then(mock -> list.iterator());
        return ret;
    }

//    ClipboardProperty mockValueProp(ValuePropertyPair pair) {
//        ClipboardProperty ret = mock(ClipboardProperty.class);
//        when(ret.toDouble()).thenReturn(pair.getRight());
//        when(ret.getStringValue()).thenReturn(String.valueOf(pair.getRight()));
//        return ret;
//    }

    public static ClipboardProperty mockValueProp(ValuePropDef def) {
        ClipboardProperty ret = mock(ClipboardProperty.class);

        List<ClipboardProperty> list = new ArrayList<>();
        for (Map.Entry<String, Double> entry : def.values.entrySet()) {
            ClipboardProperty valueProp = mock(ClipboardProperty.class);
            when(valueProp.getName()).thenReturn(entry.getKey());
            when(valueProp.toDouble()).thenReturn(entry.getValue());
            when(valueProp.getStringValue()).thenReturn(String.valueOf(entry.getValue()));
            list.add(valueProp);

            when(ret.getPropertyValue(eq(entry.getKey()))).thenReturn(valueProp);
        }

        when(ret.iterator()).then(mock -> list.iterator());
        return ret;
    }

    public static class ValuePropertyPair extends Pair<String, Double> {
        private final String name;
        private final Double value;

        public ValuePropertyPair(String name, Double value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getLeft() {
            return name;
        }

        @Override
        public Double getRight() {
            return value;
        }

        @Override
        public Double setValue(Double value) {
            return null;
        }

        public static ValuePropertyPair val(String name, Double value) {
            return new ValuePropertyPair(name, value);
        }
    }

    @Test
    void test1() {
//        ClipboardProperty prop = mockValueProp(ValuePropDef.valuePropDef("Value", ImmutableMap.of("COUNT", 12.0)));
        ClipboardPage page = mockSourceItem(
                TagPropDef.tagPropDef("Tag", Tags.of("a1", "b1")),
                ValuePropDef.valuePropDef("Value", ImmutableMap.of("COUNT", 12.0, "SUM", 10.0))
        );

        System.out.println(page.getProperty("Value(COUNT)").toDouble());
        System.out.println(page.getProperty("Value(SUM)").toDouble());
    }

}
