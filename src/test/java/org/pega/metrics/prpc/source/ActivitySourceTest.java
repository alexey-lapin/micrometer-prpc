package org.pega.metrics.prpc.source;

import com.pega.pegarules.data.internal.clipboard.ClipboardPropertyFactory;
import com.pega.pegarules.priv.PegaAPI;
import com.pega.pegarules.pub.PRRuntimeException;
import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ActivitySourceTest {

    private static final String TEST_RULE_NAME = "TestRule";
    private static final String TEST_RULE_CLASS = "Test-Rule-Class";
    private static final String TEST_AG_NAME = "TestAG:name";
    private static final String TEST_AG_NAME2 = "TestAG:name2";
    private static final String TEST_RESULTS_PROP = "pxResults";

    private PegaAPI tools;

    private ClipboardProperty resultsProp;
    private ClipboardPage primaryPage;

    @BeforeEach
    void before() {
        tools = mock(PegaAPI.class, RETURNS_DEEP_STUBS);

        resultsProp = mock(ClipboardProperty.class);
        primaryPage = mock(ClipboardPage.class);

        when(primaryPage.getProperty(TEST_RESULTS_PROP)).thenReturn(resultsProp);
    }

    @Test
    void should_builderCreatesCorrectSource_when_usingDefaultToolsSupplier() {
        ActivitySource.ActivitySourceBuilder builder = ActivitySource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .ruleName(TEST_RULE_NAME)
            .accessGroupName(TEST_AG_NAME)
            .resultsPropName(TEST_RESULTS_PROP);
        ActivitySource source = new ActivitySource(builder);

        assertAll(
            () -> assertThat(source.ruleClass()).isEqualTo(TEST_RULE_CLASS),
            () -> assertThat(source.ruleName()).isEqualTo(TEST_RULE_NAME),
            () -> assertThat(source.accessGroupName()).isEqualTo(TEST_AG_NAME),
            () -> assertThat(source.resultsPropName()).isEqualTo(TEST_RESULTS_PROP)
        );
    }

    @Test
    void should_obtainReturnsOptionalContainingResultsProp() {
        when(tools.createPage(any(), any())).thenReturn(primaryPage);

        ActivitySource.ActivitySourceBuilder builder = ActivitySource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .resultsPropName(TEST_RESULTS_PROP)
            .toolsSupplier(() -> tools);
        ActivitySource source = new ActivitySource(builder);

        assertThat(source.obtain()).containsSame(resultsProp);
    }

    @Test
    @SuppressWarnings("Unchecked")
    void should_obtainReturnsOptionalContainingPropertyWithPrimaryPage_when_noResultsPropNameProvided() {
        when(tools.createPage(any(), any())).thenReturn(primaryPage);

        ClipboardProperty wrapProp = mock(ClipboardProperty.class);
        when(wrapProp.getPageValue()).thenReturn(primaryPage);

        ClipboardPropertyFactory cpf = mock(ClipboardPropertyFactory.class, RETURNS_DEEP_STUBS);
        ClipboardPropertyFactory.setInstance(cpf);

        when(cpf.getMostSuitableClipboardObjectMocked(any(), eq('S'), any())).thenReturn(wrapProp);

        ActivitySource.ActivitySourceBuilder builder = ActivitySource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .toolsSupplier(() -> tools);
        ActivitySource source = new ActivitySource(builder);

        assertThat(source.obtain().get().getPageValue()).isSameAs(primaryPage);
    }

    @Test
    void should_obtainReturnsEmptyOptional_when_toolsInvokeActivityThrowsException() {
        doThrow(PRRuntimeException.class).when(tools).invokeActivity(any(), any(), any(), any(), any());

        ActivitySource.ActivitySourceBuilder builder = ActivitySource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .resultsPropName(TEST_RESULTS_PROP)
            .toolsSupplier(() -> tools);
        ActivitySource source = new ActivitySource(builder);

        assertThat(source.obtain()).isEmpty();
    }

    @Test
    void should_obtainReturnsEmptyOptional_when_toolsCreatePageReturnsNull() {
        when(tools.createPage(any(), any())).thenReturn(null);

        ActivitySource.ActivitySourceBuilder builder = ActivitySource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .resultsPropName(TEST_RESULTS_PROP)
            .toolsSupplier(() -> tools);
        ActivitySource source = new ActivitySource(builder);

        assertThat(source.obtain()).isEmpty();
    }

    @Test
    void should_obtainReturnsOptionalContainingResultsProp_when_removeFromClipboardThrowsException() {
        when(tools.createPage(any(), any())).thenReturn(primaryPage);
        when(primaryPage.removeFromClipboard()).thenThrow(PRRuntimeException.class);

        ActivitySource.ActivitySourceBuilder builder = ActivitySource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .resultsPropName(TEST_RESULTS_PROP)
            .toolsSupplier(() -> tools);
        ActivitySource source = new ActivitySource(builder);

        assertThat(source.obtain()).containsSame(resultsProp);
    }

    @Test
    void should_builderReturnSourceFromCache_when_sourceHasRefAndGC() {
        List<ActivitySource> sources = new ArrayList<>();

        ActivitySource source1 = ActivitySource.builder().ruleClass(TEST_RULE_CLASS).ruleName(TEST_RULE_NAME).build();
        sources.add(source1);

        ActivitySource source2 = ActivitySource.builder().ruleClass(TEST_RULE_CLASS).ruleName(TEST_RULE_NAME).build();
        sources.add(source2);

        ActivitySource source3 = ActivitySource.builder().ruleClass(TEST_RULE_CLASS).ruleName(TEST_RULE_NAME).build();
        sources.add(source3);

        ActivitySource source4 = ActivitySource.builder().ruleClass(TEST_RULE_CLASS).ruleName(TEST_RULE_NAME).build();
        sources.add(source4);

        assertThat(source1).isSameAs(source2).isSameAs(source3).isSameAs(source4);

        System.gc();
        ActivitySource source5 = ActivitySource.builder().ruleClass(TEST_RULE_CLASS).ruleName(TEST_RULE_NAME).build();

        assertThat(source1).isSameAs(source5);
    }

//    @Test
//    void should_builderBuildsNewSource_when_sourceHasNoRefAndGC() {
//        List<ActivitySource> sources = new ArrayList<>();
//
//        sources.add(ActivitySource.builder().ruleClass(TEST_RULE_CLASS).ruleName(TEST_RULE_NAME).build());
//        sources.add(ActivitySource.builder().ruleClass(TEST_RULE_CLASS).ruleName(TEST_RULE_NAME).build());
//        sources.add(ActivitySource.builder().ruleClass(TEST_RULE_CLASS).ruleName(TEST_RULE_NAME).build());
//        sources.add(ActivitySource.builder().ruleClass(TEST_RULE_CLASS).ruleName(TEST_RULE_NAME).build());
//
//        assertThat(sources.get(0)).isSameAs(sources.get(1)).isSameAs(sources.get(2)).isSameAs(sources.get(3));
//
//        int hash = sources.get(0).hashCode();
//
//        sources.clear();
//        System.gc();
//
//        ActivitySource source = ActivitySource.builder().ruleClass(TEST_RULE_CLASS).ruleName(TEST_RULE_NAME).build();
//
//        assertThat(source.hashCode()).isNotEqualTo(hash);
//    }
}