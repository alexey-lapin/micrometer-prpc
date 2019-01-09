package ru.sbrf.pegi18.mon.prpc.meter;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import ru.sbrf.pegi18.mon.prpc.source.PrpcSource;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MeterTestSupport {

    static final String VALUE_PROP_NAME_VALUE = "Value";
    static final String METER_NAME_NAME = "name";

    static PrpcSource getSourceMock(String valuePropName, Double... values) {
        List<ClipboardProperty> resultsList = new LinkedList<>();

        Arrays.asList(values).forEach(v -> {
            ClipboardProperty valueProp = mock(ClipboardProperty.class);
            when(valueProp.getStringValue()).thenReturn(String.valueOf(v));
            when(valueProp.toDouble()).thenReturn(v);

            ClipboardPage resultPage = mock(ClipboardPage.class);
            when(resultPage.getProperty(valuePropName)).thenReturn(valueProp);

            ClipboardProperty resultProp = mock(ClipboardProperty.class);
            when(resultProp.getPageValue()).thenReturn(resultPage);

            resultsList.add(resultProp);
        });

        ClipboardProperty resultsProp = mock(ClipboardProperty.class);
        when(resultsProp.iterator()).thenReturn(resultsList.iterator());
        when(resultsProp.isList()).thenReturn(true);
        when(resultsProp.size()).thenReturn(resultsList.size());

        PrpcSource source = mock(PrpcSource.class);
        when(source.collect()).thenReturn(Optional.of(resultsProp));
        return source;
    }
}
