package org.pega.metrics.prpc;

import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TagProp {

    public static ClipboardProperty of(Tags tags) {

        List<ClipboardProperty> list = new LinkedList<>();
        for (Tag tag : tags) {
            ClipboardProperty tagProp = mock(ClipboardProperty.class);
            when(tagProp.getName()).thenReturn(tag.getKey());
            when(tagProp.getStringValue()).thenReturn(tag.getValue());
            list.add(tagProp);
        }

        ClipboardProperty prop = mock(ClipboardProperty.class);
        when(prop.iterator()).then(mock -> list.iterator());
        return prop;
    }

}
