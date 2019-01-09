package ru.sbrf.pegi18.mon.prpc.source;

import com.pega.pegarules.priv.PegaAPI;
import com.pega.pegarules.pub.PRRuntimeException;
import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import com.pega.pegarules.pub.runtime.ParameterPage;
import com.pega.pegarules.session.internal.authorization.Authorization;
import com.pega.pegarules.session.internal.authorization.SessionAuthorization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DataPageSourceTest {

    private static final String TEST_RULE_NAME = "TestRule";
    private static final String TEST_RULE_CLASS = "Test-Rule-Class";
    private static final String TEST_AG_NAME = "TestAG:name";
    private static final String TEST_AG_NAME2 = "TestAG:name2";
    private static final String TEST_RESULTS_PROP = "pxResults";

    private static final ArrayList<String> AG_LIST = new ArrayList<String>() {{
        add(TEST_AG_NAME);
    }};

    private PegaAPI tools;

    private ClipboardProperty resultsProp;
    private ClipboardPage dataPage;

    private Authorization auth;
    private SessionAuthorization sauth;

    @BeforeEach
    void before() {
        tools = mock(PegaAPI.class, RETURNS_DEEP_STUBS);

        resultsProp = mock(ClipboardProperty.class);
        dataPage = mock(ClipboardPage.class);

        when(dataPage.getProperty(TEST_RESULTS_PROP)).thenReturn(resultsProp);
        when(tools.findPage(eq(TEST_RULE_NAME), (ParameterPage) any())).thenReturn(dataPage);

        auth = mock(Authorization.class);
        when(tools.getAuthorizationHandle()).thenReturn(auth);

        sauth = mock(SessionAuthorization.class);
        when(auth.getSessionAuthorization()).then(invocationOnMock -> sauth);
        when(sauth.getAvailableAccessGroups()).thenReturn(AG_LIST);
    }

    @Test
    void should_builderCreatesCorrectSource_when_usingDefaultToolsSupplier() {
        DataPageSource source = DataPageSource.builder()
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

        DataPageSource source = DataPageSource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .resultsPropName(TEST_RESULTS_PROP)
            .toolsSupplier(() -> tools)
            .build();

        assertThat(source.obtain()).containsSame(resultsProp);
    }

    @Test
    void should_obtainReturnsEmptyOptional_when_toolsFindPageReturnsNull() {
        when(tools.findPage(eq(TEST_RULE_NAME), (ParameterPage) any())).thenReturn(null);

        DataPageSource source = DataPageSource.builder()
            .toolsSupplier(() -> tools)
            .ruleName(TEST_RULE_NAME)
            .build();

        assertThat(source.obtain()).isEmpty();
    }

    @Test
    void should_obtainReturnsEmptyOptional_when_toolsFindPageThrowsException() {
        when(tools.findPage(any(), (ParameterPage) any())).thenThrow(PRRuntimeException.class);

        DataPageSource source = DataPageSource.builder()
            .toolsSupplier(() -> tools)
            .build();

        assertThat(source.obtain()).isEmpty();
    }

    @Test
    void should_collectReturnsOptionalContainingResultsProp2() {

        DataPageSource source = DataPageSource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .resultsPropName(TEST_RESULTS_PROP)
            .toolsSupplier(() -> tools)
            .build();

        assertThat(source.collect()).containsSame(resultsProp);
    }

    @Test
    void should_collectReturnsOptionalContainingResultsPropAndNotInvokeSwitchAG_when_accessGroupNotSet() throws Exception {

        when(auth.getCurrentAccessGroup()).thenReturn(TEST_AG_NAME2);

        DataPageSource source = DataPageSource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .resultsPropName(TEST_RESULTS_PROP)
            .toolsSupplier(() -> tools)
            .build();

        assertThat(source.collect()).containsSame(resultsProp);
        verify(auth, times(0)).setActiveAccessGroup(any(), any());
    }

    @Test
    void should_collectReturnsOptionalContainingResultsPropAndNotInvokeSwitchAG_when_accessGroupSetAndSameAsCurrent() throws Exception {

        when(auth.getCurrentAccessGroup()).thenReturn(TEST_AG_NAME2);

        DataPageSource source = DataPageSource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .accessGroupName(TEST_AG_NAME2)
            .resultsPropName(TEST_RESULTS_PROP)
            .toolsSupplier(() -> tools)
            .build();

        assertThat(source.collect()).containsSame(resultsProp);
        verify(auth, times(0)).setActiveAccessGroup(any(), any());
    }

    @Test
    void should_collectReturnsOptionalContainingResultsPropAndInvokeSwitchAG_when_accessGroupSetAndNotSameAsCurrent() throws Exception {

        when(auth.getCurrentAccessGroup()).thenReturn(TEST_AG_NAME2).thenReturn(TEST_AG_NAME2).thenReturn(TEST_AG_NAME).thenReturn(TEST_AG_NAME2);
        when(auth.setActiveAccessGroup(any(), any())).thenReturn(true);

        DataPageSource source = DataPageSource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .accessGroupName(TEST_AG_NAME)
            .resultsPropName(TEST_RESULTS_PROP)
            .toolsSupplier(() -> tools)
            .build();

        assertThat(source.collect()).containsSame(resultsProp);
        verify(auth, times(2)).setActiveAccessGroup(any(), any());
    }

    @Test
    void should_collectReturnsOptionalContainingResultsPropAndInvokeSwitchAG1_when_accessGroupSetAndNotSameAsCurrentAndSwitchReturnsFalse() throws Exception {

        when(auth.getCurrentAccessGroup()).thenReturn(TEST_AG_NAME2).thenReturn(TEST_AG_NAME2).thenReturn(TEST_AG_NAME).thenReturn(TEST_AG_NAME2);

        DataPageSource source = DataPageSource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .accessGroupName(TEST_AG_NAME)
            .resultsPropName(TEST_RESULTS_PROP)
            .toolsSupplier(() -> tools)
            .build();

        assertThat(source.collect()).containsSame(resultsProp);
        verify(auth, times(1)).setActiveAccessGroup(any(), any());
    }

    @Test
    void should_collectReturnsOptionalContainingResultsPropAndInvokeSwitchAG2_when_requestorIsBatch() throws Exception {

        when(tools.getThread().getRequestor().isBatchType()).thenReturn(true);
        when(auth.getCurrentAccessGroup()).thenReturn(TEST_AG_NAME2).thenReturn(TEST_AG_NAME2).thenReturn(TEST_AG_NAME).thenReturn(TEST_AG_NAME2);

        DataPageSource source = DataPageSource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .accessGroupName(TEST_AG_NAME)
            .resultsPropName(TEST_RESULTS_PROP)
            .toolsSupplier(() -> tools)
            .build();

        assertThat(source.collect()).containsSame(resultsProp);
        verify(auth, times(0)).setActiveAccessGroup(any(), any());
        verify(auth, times(2)).replaceAccessGroup(any(), any());
    }

    @Test
    void should_collectReturnsOptionalContainingResultsPropAndInvokeSwitchAG1_when_setActiveAccessGroupThrows() throws Exception {

        when(auth.setActiveAccessGroup(any(), any())).thenThrow(PRRuntimeException.class);
        when(auth.getCurrentAccessGroup()).thenReturn(TEST_AG_NAME2).thenReturn(TEST_AG_NAME2).thenReturn(TEST_AG_NAME).thenReturn(TEST_AG_NAME2);

        DataPageSource source = DataPageSource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .accessGroupName(TEST_AG_NAME)
            .resultsPropName(TEST_RESULTS_PROP)
            .toolsSupplier(() -> tools)
            .build();

        assertThat(source.collect()).containsSame(resultsProp);
        verify(auth, times(1)).setActiveAccessGroup(any(), any());
    }

    @Test
    void should_collectReturnsOptionalContainingResultsPropAndInvokeSwitchAG1_when_getCurrentAccessGroupThrows() throws Exception {

        when(auth.getCurrentAccessGroup()).thenThrow(PRRuntimeException.class);

        DataPageSource source = DataPageSource.builder()
            .ruleClass(TEST_RULE_CLASS)
            .accessGroupName(TEST_AG_NAME)
            .resultsPropName(TEST_RESULTS_PROP)
            .toolsSupplier(() -> tools)
            .build();

        assertThat(source.collect()).containsSame(resultsProp);
        verify(auth, times(0)).setActiveAccessGroup(any(), any());
    }
}