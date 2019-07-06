package org.pega.metrics.prpc.source;

import com.pega.pegarules.priv.PegaAPI;
import com.pega.pegarules.pub.PRRuntimeException;
import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import com.pega.pegarules.pub.context.PRThread;
import com.pega.pegarules.pub.context.ThreadContainer;
import com.pega.pegarules.pub.database.DatabaseException;
import com.pega.pegarules.pub.runtime.ParameterPage;
import io.micrometer.core.instrument.Tags;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.pega.metrics.prpc.TagProp;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SqlSourceTest {

    private static final String PROP_PXRESULTS = "pxResults";

    private static final String CONST_QUERY1 = "query1";
    private static final String CONST_QUERY2 = "query2";

    private PegaAPI tools;

    @BeforeEach
    void before() {
        tools = mock(PegaAPI.class, RETURNS_DEEP_STUBS);
    }

    @AfterEach
    void after() {
        ThreadContainer.clear();
    }

    @Test
    void should_collectReturnsOptionalContainingResultsProp() throws Exception {
        ClipboardProperty resultsProp = mock(ClipboardProperty.class);
        ClipboardPage browsePage = mock(ClipboardPage.class);

        assertThat(browsePage.getProperty(PROP_PXRESULTS)).isNull();

        when(tools.createPage(any(), any())).thenReturn(browsePage);
        when(tools.getDatabase().executeRDB((String) any(), any())).then(invocationOnMock -> {
            when(browsePage.getProperty(PROP_PXRESULTS)).thenReturn(resultsProp);
            return 0;
        });

        SqlSource.SqlSourceBuilder builder = SqlSource.builder().toolsSupplier(() -> tools);
        SqlSource source = new SqlSource(builder);

        assertThat(source.collect()).containsSame(resultsProp);
    }

    @Nested
    class GroupingTest {

        SqlSource source = new SqlSource(SqlSource.builder().toolsSupplier(() -> tools));
        ClipboardProperty targetProp = null;

        @Test
        void should_beNoChangesToTargetProp_when_sourcePropIsNull() {
            source.groupResults(null, targetProp);

            assertThat(targetProp).isNull();
        }

        @Test
        void should_addPagesToTargetProp_when_sourcePropIsList() {
            ClipboardProperty tagProp1 = TagProp.of(Tags.of("name1", "val1"));
            ClipboardProperty tagProp2 = TagProp.of(Tags.of("name1", "val2"));

            ClipboardProperty sourceItem1 = mock(ClipboardProperty.class);
            when(sourceItem1.getProperty("Tag")).thenReturn(tagProp1);

            ClipboardProperty sourceItem2 = mock(ClipboardProperty.class);
            when(sourceItem2.getProperty("Tag")).thenReturn(tagProp2);

            ClipboardProperty sourceProp = mock(ClipboardProperty.class);
            when(sourceProp.isList()).thenReturn(true);
            when(sourceProp.iterator()).then(mock -> Arrays.asList(sourceItem1, sourceItem2).iterator());

            Map<String, ClipboardProperty> targetMap = new HashMap<>();
            targetProp = mock(ClipboardProperty.class);
            doAnswer(mock -> targetMap.put(mock.getArgument(0), mock.getArgument(1))).when(targetProp).add(anyString(), any());

            source.groupResults(sourceProp, targetProp);

            assertThat(targetMap).containsValue(sourceItem1).containsValue(sourceItem2);
        }

        @Test
        void should_addPagesToTargetProp_when_sourcePropIsGroup() {
            ClipboardProperty tagProp1 = TagProp.of(Tags.of("name1", "val1"));
            ClipboardProperty tagProp2 = TagProp.of(Tags.of("name1", "val2"));

            ClipboardProperty sourceItem1 = mock(ClipboardProperty.class);
            when(sourceItem1.getProperty("Tag")).thenReturn(tagProp1);

            ClipboardProperty sourceItem2 = mock(ClipboardProperty.class);
            when(sourceItem2.getProperty("Tag")).thenReturn(tagProp2);

            ClipboardProperty sourceProp = mock(ClipboardProperty.class);
            when(sourceProp.isGroup()).thenReturn(true);
            when(sourceProp.iterator()).then(mock -> Arrays.asList(sourceItem1, sourceItem2).iterator());

            Map<String, ClipboardProperty> targetMap = new HashMap<>();
            targetProp = mock(ClipboardProperty.class);
            doAnswer(mock -> targetMap.put(mock.getArgument(0), mock.getArgument(1))).when(targetProp).add(anyString(), any());

            source.groupResults(sourceProp, targetProp);

            assertThat(targetMap).containsValue(sourceItem1).containsValue(sourceItem2);
        }

        @Test
        void should_beNoChangesToTargetProp_when_sourcePropIsEmptyGroup() {
            ClipboardProperty sourceProp = mock(ClipboardProperty.class);
            when(sourceProp.isGroup()).thenReturn(true);
            when(sourceProp.isEmpty()).thenReturn(true);

            source.groupResults(sourceProp, targetProp);

            assertThat(targetProp).isNull();
        }

        @Test
        void should_beNoChangesToTargetProp_when_sourcePropIsEmptyList() {
            ClipboardProperty sourceProp = mock(ClipboardProperty.class);
            when(sourceProp.isList()).thenReturn(true);
            when(sourceProp.isEmpty()).thenReturn(true);

            source.groupResults(sourceProp, targetProp);

            assertThat(targetProp).isNull();
        }
    }

    @Test
    void name5() throws DatabaseException {
        ClipboardProperty resultsProp = mock(ClipboardProperty.class);
        when(resultsProp.isList()).thenReturn(true);
        when(resultsProp.iterator()).then(mock -> Collections.emptyIterator());

        ClipboardProperty groupProp = mock(ClipboardProperty.class);

        ClipboardPage browsePage = mock(ClipboardPage.class);
        when(browsePage.getIfPresent(PROP_PXRESULTS)).thenReturn(resultsProp);
        when(browsePage.getProperty("pxPages")).thenReturn(groupProp);

        when(tools.createPage(any(), any())).thenReturn(browsePage);
        when(tools.getDatabase().executeRDB((String) any(), any())).then(invocationOnMock -> {
            return 0;
        });

        SqlSource.SqlSourceBuilder builder = SqlSource.builder()
            .toolsSupplier(() -> tools)
            .groupPropName("pxPages");
        SqlSource source = new SqlSource(builder);

        assertThat(source.collect()).containsSame(groupProp);
    }

    @Test
    void should_collectReturnsOptionalContainingResultsProp_when_removeFromClipboardThrowsException() throws Exception {
        ClipboardProperty resultsProp = mock(ClipboardProperty.class);
        ClipboardPage browsePage = mock(ClipboardPage.class);
        when(browsePage.removeFromClipboard()).thenThrow(PRRuntimeException.class);

        assertThat(browsePage.getProperty(PROP_PXRESULTS)).isNull();

        when(tools.createPage(any(), any())).thenReturn(browsePage);
        when(tools.getDatabase().executeRDB((String) any(), any())).then(invocationOnMock -> {
            when(browsePage.getProperty(PROP_PXRESULTS)).thenReturn(resultsProp);
            return 0;
        });

        SqlSource.SqlSourceBuilder builder = SqlSource.builder()
            .toolsSupplier(() -> tools);
        SqlSource source = new SqlSource(builder);

        assertThat(source.collect()).containsSame(resultsProp);
    }

    @Test
    void should_collectReturnsEmptyOptional_when_databaseExecuteRDBThrowsException() throws Exception {
        when(tools.getDatabase().executeRDB((String) any(), any())).thenThrow(PRRuntimeException.class);

        SqlSource.SqlSourceBuilder builder = SqlSource.builder()
            .toolsSupplier(() -> tools);
        SqlSource source = new SqlSource(builder);

        assertThat(source.collect()).isEmpty();
    }

    @Test
    void should_collectReturnsEmptyOptional_when_toolsCreatePageThrowsException() {
        when(tools.createPage(any(), any())).thenThrow(PRRuntimeException.class);

        SqlSource.SqlSourceBuilder builder = SqlSource.builder()
            .toolsSupplier(() -> tools);
        SqlSource source = new SqlSource(builder);

        assertThat(source.collect()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_builderCreatesCorrectSource_when_usingDefaultToolsSupplier() {
        int maxRecords = 10;
        String queryString = CONST_QUERY1;
        ParameterPage pp = new ParameterPage();
        pp.putString("key", "value");
        String tagsPropName = "TagProp";

        PRThread thread = mock(PRThread.class);
        when(thread.getPublicAPI()).thenReturn(tools);

        ThreadContainer.put(thread);

        SqlSource.SqlSourceBuilder builder = SqlSource.builder()
            .parameterPage(pp)
            .maxRecords(maxRecords)
            .queryString(queryString)
            .tagsPropName(tagsPropName);
        SqlSource source = new SqlSource(builder);

        assertAll(
            () -> assertThat(source.tools()).isSameAs(tools),
            () -> assertThat(source.parameterPage()).isNotSameAs(pp).isEqualTo(pp),
            () -> assertThat(source.maxRecords()).isEqualTo(maxRecords),
            () -> assertThat(source.queryString()).isEqualTo(queryString),
            () -> assertThat(source.tagsPropName()).isEqualTo(tagsPropName)
        );
    }

    @Test
    void should_builderCreatesCorrectSource_when_usingNonDefaultToolsSupplier() {
        SqlSource.SqlSourceBuilder builder = SqlSource.builder()
            .toolsSupplier(() -> tools);
        SqlSource source = new SqlSource(builder);

        assertThat(source.tools()).isSameAs(tools);
    }

    @Test
    void should_builderReturnSourceFromCache_when_sourceHasRefAndGC() {
        List<SqlSource> sources = new ArrayList<>();

        SqlSource source1 = SqlSource.builder().queryString(CONST_QUERY1).build();
        sources.add(source1);

        SqlSource source2 = SqlSource.builder().queryString(CONST_QUERY1).build();
        sources.add(source2);

        SqlSource source3 = SqlSource.builder().queryString(CONST_QUERY1).build();
        sources.add(source3);

        SqlSource source4 = SqlSource.builder().queryString(CONST_QUERY1).build();
        sources.add(source4);

        assertThat(source1).isSameAs(source2).isSameAs(source3).isSameAs(source4);

        System.gc();
        SqlSource source5 = SqlSource.builder().queryString(CONST_QUERY1).build();

        assertThat(source1).isSameAs(source5);
    }

    @Test
    void should_builderBuildsNewSource_when_noRefToInstanceAndGC() {
        List<SqlSource> sources = new ArrayList<>();

        sources.add(SqlSource.builder().queryString(CONST_QUERY1).build());
        sources.add(SqlSource.builder().queryString(CONST_QUERY1).build());
        sources.add(SqlSource.builder().queryString(CONST_QUERY1).build());
        sources.add(SqlSource.builder().queryString(CONST_QUERY1).build());

        assertThat(sources.get(0)).isSameAs(sources.get(1)).isSameAs(sources.get(2)).isSameAs(sources.get(3));

        int hash = sources.get(0).hashCode();

        sources.clear();
        System.gc();

        SqlSource source = SqlSource.builder().queryString(CONST_QUERY1).build();

        assertThat(source.hashCode()).isNotEqualTo(hash);
    }

    @Test
    void should_builderBeEqualCorrectly() {
        SqlSource.SqlSourceBuilder builder = SqlSource.builder().queryString(CONST_QUERY1);
        assertAll(
            () -> assertThat(SqlSource.builder().queryString(CONST_QUERY1)).isNotEqualTo(null),
            () -> assertThat(SqlSource.builder().queryString(CONST_QUERY1)).isNotEqualTo(""),
            () -> assertThat(SqlSource.builder().queryString(CONST_QUERY1)).isNotEqualTo(SqlSource.builder().queryString(CONST_QUERY2)),
            () -> assertThat(SqlSource.builder().queryString(CONST_QUERY1)).isEqualTo(SqlSource.builder().queryString(CONST_QUERY1)),
            () -> assertThat(builder).isEqualTo(builder)
        );
    }

    @Nested
    class ValueSupplier {

        @Test
        void should_constructCachingSource_when_expirationDurationIsSpecified() {
            ClipboardProperty prop1 = mock(ClipboardProperty.class);
            ClipboardProperty prop2 = mock(ClipboardProperty.class);

            ClipboardPage page = mock(ClipboardPage.class);
            when(page.getProperty(PROP_PXRESULTS)).thenReturn(prop1).thenReturn(prop2);

            when(tools.createPage(any(), any())).thenReturn(page);

            SqlSource.SqlSourceBuilder builder = SqlSource.builder()
                .toolsSupplier(() -> tools)
                .expirationDuration(100)
                .expirationTimeUnit(TimeUnit.SECONDS);
            SqlSource source = new SqlSource(builder);

            assertThat(source.get().get()).isSameAs(source.get().get()).isSameAs(prop1);
            assertThat(source.collect().get()).isSameAs(prop2);
        }

        @Test
        void should_constructDirectSource_when_expirationDurationIsNotSpecified() {
            ClipboardProperty prop1 = mock(ClipboardProperty.class);
            ClipboardProperty prop2 = mock(ClipboardProperty.class);

            ClipboardPage page = mock(ClipboardPage.class);
            when(page.getProperty(PROP_PXRESULTS)).thenReturn(prop1).thenReturn(prop2);

            when(tools.createPage(any(), any())).thenReturn(page);

            SqlSource.SqlSourceBuilder builder = SqlSource.builder()
                .toolsSupplier(() -> tools);
            SqlSource source = new SqlSource(builder);

            assertThat(source.get().get()).isNotSameAs(source.get().get());
        }
    }
}