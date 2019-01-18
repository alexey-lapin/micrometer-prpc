package ru.sbrf.pegi18.mon.prpc.meter;

import com.pega.pegarules.pub.PRRuntimeException;
import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import ru.sbrf.pegi18.mon.prpc.source.PrpcSource;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MeterTestSupport {

    public static final String VALUE_PROP_NAME_VALUE = "Value";
    public static final String METER_NAME_NAME = "name";

//    static ClipboardProperty getResultsPropMock

    public static PrpcSource getSourceMock(String valuePropName, Double... values) {
        List<ClipboardProperty> resultsList = new LinkedList<>();

        Arrays.asList(values).forEach(v -> {
            ClipboardProperty resultProp = getResultPropMock(valuePropName, v);
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

    private static ClipboardProperty getResultPropMock(String valuePropName, Double value) {
        ClipboardProperty valueProp = mock(ClipboardProperty.class);
        when(valueProp.getStringValue()).thenReturn(String.valueOf(value));
        when(valueProp.toDouble()).thenReturn(value);

        ClipboardProperty oneTagProp = mock(ClipboardProperty.class);
        when(oneTagProp.getName()).thenReturn("tagName");
        when(oneTagProp.getStringValue()).thenReturn("tagValue");
        List<ClipboardProperty> list = new ArrayList<>();
        list.add(oneTagProp);

        ClipboardProperty tagProp = mock(ClipboardProperty.class);
        when(tagProp.iterator()).thenReturn(list.iterator());

        ClipboardPage resultPage = mock(ClipboardPage.class);
        when(resultPage.getProperty(valuePropName)).thenReturn(valueProp);
        when(resultPage.getProperty("Tag")).thenReturn(tagProp);
        when(resultPage.getIfPresent("Tag")).thenReturn(tagProp);

        ClipboardProperty resultProp = mock(ClipboardProperty.class);
        when(resultProp.getPageValue()).thenReturn(resultPage);
        return resultProp;
    }

    //    static PrpcSource getListSourceMock(String valuePropName, Double... values) {
//        PrpcSource source = getSourceMock(valuePropName, values);
//        when(source
//    }
    static PrpcSource getPageSourceMock(String valuePropName, Double value) {
        ClipboardProperty resultsProp = getResultPropMock(valuePropName, value);
        when(resultsProp.isPage()).thenReturn(true);
        when(resultsProp.size()).thenReturn(1);

        PrpcSource source = mock(PrpcSource.class);
        when(source.collect()).thenReturn(Optional.of(resultsProp));
        return source;
    }

    static PrpcSource getThrowingSource() {
        PrpcSource source = mock(PrpcSource.class);
        when(source.collect()).thenThrow(PRRuntimeException.class);
        return source;
    }
}
