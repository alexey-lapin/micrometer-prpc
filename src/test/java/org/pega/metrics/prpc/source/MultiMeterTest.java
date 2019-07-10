package org.pega.metrics.prpc.source;

import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import org.junit.jupiter.api.Test;
import org.pega.metrics.prpc.TagProp;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MultiMeterTest {

    public static final String PROP_NAME = "prop";

    @Test
    void should_returnEmptyIterable_when_sourceIsEmpty() {
        AbstractPrpcSource source = mock(AbstractPrpcSource.class);
        when(source.get()).thenReturn(Optional.empty());

        Iterable<MultiGauge.Row<?>> rows = MultiMeter.rows(source, PROP_NAME);

        assertThat(rows).isEmpty();
    }

    @Test
    void should_returnCorrectRowsIterable_when_sourceIsGroup() {
        Tags tags1 = Tags.of("name1", "val1", "name2", "val2");
        ClipboardProperty sourceItem1 = mock(ClipboardProperty.class);
        ClipboardProperty tagProp1 = TagProp.of(tags1);
        when(sourceItem1.getProperty("Tag")).thenReturn(tagProp1);

        Tags tags2 = Tags.of("name1", "val3", "name2", "val4");
        ClipboardProperty sourceItem2 = mock(ClipboardProperty.class);
        ClipboardProperty tagProp2 = TagProp.of(tags2);
        when(sourceItem2.getProperty("Tag")).thenReturn(tagProp2);

        ClipboardProperty prop = mock(ClipboardProperty.class);
        when(prop.isGroup()).thenReturn(true);
        when(prop.iterator()).then(mock -> Arrays.asList(sourceItem1, sourceItem2).iterator());

        AbstractPrpcSource source = mock(AbstractPrpcSource.class);
        when(source.get()).thenReturn(Optional.of(prop));
        when(source.tagsPropName()).thenReturn("Tag");

        Iterable<MultiGauge.Row<?>> rows = MultiMeter.rows(source, PROP_NAME);

        assertThat(rows).extracting("uniqueTags", "obj").contains(tuple(tags1, source)).contains(tuple(tags2, source));
    }

    @Test
    void shouldReturnEmptyIterable_when_sourceIsNotGroup() {
        ClipboardProperty prop = mock(ClipboardProperty.class);

        AbstractPrpcSource source = mock(AbstractPrpcSource.class);
        when(source.get()).thenReturn(Optional.of(prop));

        Iterable<MultiGauge.Row<?>> rows = MultiMeter.rows(source, PROP_NAME);

        assertThat(rows).isEmpty();
    }

    @Test
    void shouldReturnEmptyIterable_when_sourceIsEmptyGroup() {
        ClipboardProperty prop = mock(ClipboardProperty.class);
        when(prop.isGroup()).thenReturn(true);
        when(prop.isEmpty()).thenReturn(true);

        AbstractPrpcSource source = mock(AbstractPrpcSource.class);
        when(source.get()).thenReturn(Optional.of(prop));

        Iterable<MultiGauge.Row<?>> rows = MultiMeter.rows(source, PROP_NAME);

        assertThat(rows).isEmpty();
    }
}