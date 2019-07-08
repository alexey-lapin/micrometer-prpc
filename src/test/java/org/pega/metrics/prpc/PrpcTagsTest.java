package org.pega.metrics.prpc;

import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.GraphLayout;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrpcTagsTest {

    private static final String TAG_NAME_1 = "name1";
    private static final String TAG_VAL_1 = "val1";
    private static final String TAG_NAME_2 = "name2";
    private static final String TAG_VAL_2 = "val2";

    @Test
    void should_constructCorrectTagsObjectFromPrperty() {
        ClipboardProperty tagProp1 = mock(ClipboardProperty.class);
        when(tagProp1.getName()).thenReturn(TAG_NAME_1);
        when(tagProp1.getStringValue()).thenReturn(TAG_VAL_1);

        ClipboardProperty tagProp2 = mock(ClipboardProperty.class);
        when(tagProp2.getName()).thenReturn(TAG_NAME_2);
        when(tagProp2.getStringValue()).thenReturn(TAG_VAL_2);

        ClipboardProperty prop = mock(ClipboardProperty.class);
        when(prop.iterator()).thenAnswer(mock -> Arrays.asList(tagProp1, tagProp2).iterator());

        assertThat(PrpcTags.of(prop)).isEqualTo(Tags.of(TAG_NAME_1, TAG_VAL_1, TAG_NAME_2, TAG_VAL_2));
    }

    @Test
    void should_idForEqualTagsObjectsBeTheSame() {
        Tags tags1 = Tags.of(TAG_NAME_1, TAG_VAL_1);
        Tags tags2 = Tags.of(TAG_NAME_1, TAG_VAL_1);

        assertThat(tags1).isNotSameAs(tags2);
        assertThat(tags1).isEqualTo(tags2);
        assertThat(PrpcTags.id(tags1)).isSameAs(PrpcTags.id(tags2));
    }

    @Test
    void should_idForTagsObjectAndIdForPropertyBeTheSame() {
        ClipboardProperty tagProp1 = mock(ClipboardProperty.class);
        when(tagProp1.getName()).thenReturn(TAG_NAME_1);
        when(tagProp1.getStringValue()).thenReturn(TAG_VAL_1);

        ClipboardProperty tagProp2 = mock(ClipboardProperty.class);
        when(tagProp2.getName()).thenReturn(TAG_NAME_2);
        when(tagProp2.getStringValue()).thenReturn(TAG_VAL_2);

        ClipboardProperty prop = mock(ClipboardProperty.class);
        when(prop.iterator()).thenAnswer(mock -> Arrays.asList(tagProp1, tagProp2).iterator());

        Tags tags1 = Tags.of(TAG_NAME_1, TAG_VAL_1, TAG_NAME_2, TAG_VAL_2);

        assertThat(PrpcTags.id(tags1)).isSameAs(PrpcTags.id(prop));

    }

    @Test
    void should_returnEmptyTagsForNullProperty() {
        assertThat(PrpcTags.of(null)).isEqualTo(Tags.empty());
    }

    @Test
    void should_returnCache() {
        assertThat(PrpcTags.getCache()).isNotNull();
    }

    @Test
    @Disabled
        // The way to investigate cache memory footprint
    void memoryBenchmark() {
        for (int i = 0; i < 20000; i++) {
            PrpcTags.id(Tags.of(
                Tag.of("some_tag_name_first_" + String.format("%05d", i), "some_tage_value_first_" + String.format("%05d", i)),
                Tag.of("some_tag_name_second_" + String.format("%05d", i), "some_tag_value_second_" + String.format("%05d", i)),
                Tag.of("some_tag_name_third_" + String.format("%05d", i), "some_tag_value_third_" + String.format("%05d", i))));
        }
        String footprint = GraphLayout.parseInstance(PrpcTags.getCache()).toFootprint();
        System.out.println(footprint);
    }
}