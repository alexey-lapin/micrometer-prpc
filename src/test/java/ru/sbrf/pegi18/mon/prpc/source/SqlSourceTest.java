package ru.sbrf.pegi18.mon.prpc.source;

import com.pega.pegarules.priv.PegaAPI;
import com.pega.pegarules.pub.PRRuntimeException;
import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import com.pega.pegarules.pub.context.PRThread;
import com.pega.pegarules.pub.context.ThreadContainer;
import com.pega.pegarules.pub.runtime.ParameterPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SqlSourceTest {

    private static final String PROP_PXRESULTS = "pxResults";

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
        String queryString = "query";
        ParameterPage pp = new ParameterPage();
        pp.putString("key", "value");

        PRThread thread = mock(PRThread.class);
        when(thread.getPublicAPI()).thenReturn(tools);

        ThreadContainer.put(thread);

        SqlSource.SqlSourceBuilder builder = SqlSource.builder()
            .parameterPage(pp)
            .maxRecords(maxRecords)
            .queryString(queryString);
        SqlSource source = new SqlSource(builder);

        assertAll(
            () -> assertThat(source.tools()).isSameAs(tools),
            () -> assertThat(source.parameterPage()).isNotSameAs(pp).isEqualTo(pp),
            () -> assertThat(source.maxRecords()).isEqualTo(maxRecords),
            () -> assertThat(source.queryString()).isEqualTo(queryString)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_builderCreatesCorrectSource_when_usingNonDefaultToolsSupplier() {
        SqlSource.SqlSourceBuilder builder = SqlSource.builder()
            .toolsSupplier(() -> tools);
        SqlSource source = new SqlSource(builder);

        assertThat(source.tools()).isSameAs(tools);
    }

//    @Test
//    void vv() {
//        List<SqlSource> sources = new ArrayList<>();
//
//        SqlSource source1 = SqlSource.builder().queryString("query").build();
//        sources.add(source1);
//        SqlSource source2 = SqlSource.builder().queryString("query").build();
//        sources.add(source2);
//        SqlSource source3 = SqlSource.builder().queryString("query").build();
//        sources.add(source3);
//        SqlSource source4 = SqlSource.builder().queryString("query").build();
//        sources.add(source4);
//
//        assertThat(source1).isSameAs(source2).isSameAs(source3).isSameAs(source4);
//
////        System.gc();
////        SqlSource source5 = SqlSource.builder().queryString("query").build();
////
////        assertThat(source1).isSameAs(source5);
//    }
}