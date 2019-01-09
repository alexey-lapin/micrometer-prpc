package ru.sbrf.pegi18.mon.prpc.source;

import com.pega.pegarules.priv.PegaAPI;
import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
    private ClipboardPage dataPage;

    @BeforeEach
    void before() {
        tools = mock(PegaAPI.class, RETURNS_DEEP_STUBS);

        resultsProp = mock(ClipboardProperty.class);
        dataPage = mock(ClipboardPage.class);

        when(dataPage.getProperty(TEST_RESULTS_PROP)).thenReturn(resultsProp);
//        when(tools.findPage(eq(TEST_RULE_NAME), (ParameterPage) any())).thenReturn(dataPage);
//
//        auth = mock(Authorization.class);
//        when(tools.getAuthorizationHandle()).thenReturn(auth);
//
//        sauth = mock(SessionAuthorization.class);
//        when(auth.getSessionAuthorization()).then(invocationOnMock -> sauth);
//        when(sauth.getAvailableAccessGroups()).thenReturn(AG_LIST);
    }

    @Test
    void should_builderCreatesCorrectSource_when_usingDefaultToolsSupplier() {
        ActivitySource source = ActivitySource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .ruleName(TEST_RULE_NAME)
            .accessGroupName(TEST_AG_NAME)
            .resultsPropName(TEST_RESULTS_PROP)
            .build();

        assertAll(
            () -> assertThat(source.ruleClass()).isEqualTo(TEST_RULE_CLASS),
            () -> assertThat(source.ruleName()).isEqualTo(TEST_RULE_NAME),
            () -> assertThat(source.accessGroupName()).isEqualTo(TEST_AG_NAME),
            () -> assertThat(source.resultsPropName()).isEqualTo(TEST_RESULTS_PROP)
        );
    }

    @Test
    void should_obtainReturnsOptionalContainingResultsProp() {

//        when(tools.invokeActivity(any(), any(), any(), any(), any())));
        when(tools.createPage(any(), any())).thenReturn(dataPage);
//        doAnswer(invocationOnMock -> )

        ActivitySource source = ActivitySource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .resultsPropName(TEST_RESULTS_PROP)
            .toolsSupplier(() -> tools)
            .build();

        assertThat(source.obtain()).containsSame(resultsProp);
    }

}