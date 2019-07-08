package org.pega.metrics.prpc.source;

import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.Tags;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PrpcCallbackTest {


    @Test
    void should_returnNaN_when_sourceIsEmpty() {
        PrpcSource source = mock(PrpcSource.class);

        PrpcCallback callback = PrpcCallback.weak("prop");

        double result = callback.applyAsDouble(source);

        assertThat(result).isNaN();
    }

    @Test
    void should_returnNaN_when_sourceIsNetherGroupNorPage() {
        ClipboardProperty prop = mock(ClipboardProperty.class);

        PrpcSource source = mock(PrpcSource.class);
        when(source.get()).thenReturn(Optional.of(prop));

        PrpcCallback callback = PrpcCallback.weak("prop");

        double result = callback.applyAsDouble(source);

        assertThat(result).isNaN();
    }

    @Test
    void should_returnNaN_when_bodyThrows() {
        ClipboardProperty prop = mock(ClipboardProperty.class);
        when(prop.isGroup()).thenThrow(RuntimeException.class);

        PrpcSource source = mock(PrpcSource.class);
        when(source.get()).thenReturn(Optional.of(prop));

        PrpcCallback callback = PrpcCallback.weak("prop");

        double result = callback.applyAsDouble(source);

        assertThat(result).isNaN();
    }

    @Test
    void should_returnNaN_when_sourceIsEmptyGroup() {
        ClipboardProperty prop = mock(ClipboardProperty.class);
        when(prop.isGroup()).thenReturn(true);
        when(prop.isEmpty()).thenReturn(true);

        PrpcSource source = mock(PrpcSource.class);
        when(source.get()).thenReturn(Optional.of(prop));

        PrpcCallback callback = PrpcCallback.weak("prop");

        double result = callback.applyAsDouble(source);

        assertThat(result).isNaN();
    }

    @Test
    void should_returnNaN_when_sourceIsGroup() {
        ClipboardProperty valueProp = mock(ClipboardProperty.class);
        when(valueProp.toDouble()).thenReturn(6.2);

        ClipboardPage page = mock(ClipboardPage.class);
        when(page.getIfPresent(any())).thenReturn(valueProp);

        ClipboardProperty prop = mock(ClipboardProperty.class);
        when(prop.isGroup()).thenReturn(true);
        when(prop.getPageValue(any())).thenReturn(page);

        PrpcSource source = mock(PrpcSource.class);
        when(source.get()).thenReturn(Optional.of(prop));

        PrpcCallback callback = PrpcCallback.weak(Tags.empty(), "prop");

        double result = callback.applyAsDouble(source);

        assertThat(result).isEqualTo(6.2);
    }

    @Test
    void should_returnNaN_when_sourceIsPageWithoutValueProp() {
        ClipboardPage page = mock(ClipboardPage.class);

        ClipboardProperty prop = mock(ClipboardProperty.class);
        when(prop.isPage()).thenReturn(true);
        when(prop.getPageValue()).thenReturn(page);

        PrpcSource source = mock(PrpcSource.class);
        when(source.get()).thenReturn(Optional.of(prop));

        PrpcCallback callback = PrpcCallback.weak("prop");

        double result = callback.applyAsDouble(source);

        assertThat(result).isNaN();
    }

    @Test
    void should_returnNumber_when_sourceIsPageWithValueProp() {
        ClipboardProperty valueProp = mock(ClipboardProperty.class);
        when(valueProp.toDouble()).thenReturn(6.1);

        ClipboardPage page = mock(ClipboardPage.class);
        when(page.getIfPresent("prop")).thenReturn(valueProp);

        ClipboardProperty prop = mock(ClipboardProperty.class);
        when(prop.isPage()).thenReturn(true);
        when(prop.getPageValue()).thenReturn(page);

        PrpcSource source = mock(PrpcSource.class);
        when(source.get()).thenReturn(Optional.of(prop));

        PrpcCallback callback = PrpcCallback.weak("prop");

        double result = callback.applyAsDouble(source);

        assertThat(result).isEqualTo(6.1);
    }

    @Test
    void should_createCorrectCallbacks() {
        Tags tags = Tags.of("name1", "val1");

        PrpcCallback callback;

        callback = PrpcCallback.weak("prop");

        assertThat(callback)
            .hasFieldOrPropertyWithValue("source", null)
            .hasFieldOrPropertyWithValue("tags", null)
            .hasFieldOrPropertyWithValue("valuePropName", "prop");

        callback = PrpcCallback.weak(tags, "prop");

        assertThat(callback)
            .hasFieldOrPropertyWithValue("source", null)
            .hasFieldOrPropertyWithValue("tags", tags)
            .hasFieldOrPropertyWithValue("valuePropName", "prop");

        PrpcSource source = mock(PrpcSource.class);

        callback = PrpcCallback.strong(source, "prop");

        assertThat(callback)
            .hasFieldOrPropertyWithValue("source", source)
            .hasFieldOrPropertyWithValue("tags", null)
            .hasFieldOrPropertyWithValue("valuePropName", "prop");

        callback = PrpcCallback.strong(source, tags, "prop");

        assertThat(callback)
            .hasFieldOrPropertyWithValue("source", source)
            .hasFieldOrPropertyWithValue("tags", tags)
            .hasFieldOrPropertyWithValue("valuePropName", "prop");
    }
}
