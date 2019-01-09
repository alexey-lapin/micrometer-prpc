package ru.sbrf.pegi18.mon.prpc.source;

import com.pega.pegarules.data.internal.clipboard.ClipboardPropertyFactory;
import com.pega.pegarules.priv.authorization.PegaAuthorization;
import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Alexey Lapin
 */
public abstract class AbstractRulePrpcSource extends AbstractPrpcSource {

    private final String ruleClass;
    private final String ruleName;
    private final String accessGroupName;
    private final String resultsPropName;

    protected AbstractRulePrpcSource(AbstractRulePrpcSourceBuilder<?> builder) {
        super(builder);
        this.ruleClass = builder.ruleClass;
        this.ruleName = builder.ruleName;
        this.accessGroupName = builder.accessGroupName;
        this.resultsPropName = builder.resultsPropName;
    }

    public String ruleClass() {
        return ruleClass;
    }

    public String ruleName() {
        return ruleName;
    }

    public String accessGroupName() {
        return accessGroupName;
    }

    public String resultsPropName() {
        return resultsPropName;
    }

    @Override
    public Optional<ClipboardProperty> collect() {
        if (logger.isDebugEnabled()) {
            logger.debug(toString() + " collect using executable " + tools());
        }

        String currentAccessGroup = getCurrentAccessGroup();
        boolean switched = false;
        if (shouldSwitchAccessGroup(currentAccessGroup)) {
            switched = switchAccessGroup(accessGroupName());
        }

        Optional<ClipboardProperty> resultsProp = obtain();

        if (switched && shouldSwitchAccessGroup(currentAccessGroup)) {
            switchAccessGroup(currentAccessGroup);
        }

        return resultsProp;
    }

    private boolean shouldSwitchAccessGroup(String currentAccessGroup) {
        return !StringUtils.isBlank(accessGroupName())
            && !StringUtils.isBlank(currentAccessGroup)
            && !currentAccessGroup.equals(accessGroupName());
    }

    protected abstract Optional<ClipboardProperty> obtain();

    public abstract static class AbstractRulePrpcSourceBuilder<T extends AbstractRulePrpcSourceBuilder<T>> extends AbstractPrpcSourceBuilder<T> {
        private String ruleClass;
        private String ruleName;
        private String accessGroupName;
        private String resultsPropName;

        public T ruleClass(String ruleClass) {
            this.ruleClass = ruleClass;
            return self();
        }

        public T ruleName(String ruleName) {
            this.ruleName = ruleName;
            return self();
        }

        public T accessGroupName(String accessGroupName) {
            this.accessGroupName = accessGroupName;
            return self();
        }

        public T resultsPropName(String resultsPropName) {
            this.resultsPropName = resultsPropName;
            return self();
        }
    }

    protected ClipboardProperty wrap(ClipboardPage page) {
        ClipboardPage top = tools().createPage("@baseclass", null);
        ClipboardProperty prop = ClipboardPropertyFactory.getMostSuitableClipboardObject("Results", 'S', top);
        prop.setValue(page);
        top.removeFromClipboard();
        return prop;
    }

    @SuppressWarnings("unchecked")
    private boolean switchAccessGroup(String name) {
        boolean result = false;

        PegaAuthorization auth = (PegaAuthorization) tools().getAuthorizationHandle();
        boolean isBatch = tools().getThread().getRequestor().isBatchType();

        String currentAccessGroup = null;

        try {
            currentAccessGroup = getCurrentAccessGroup();

            Object sauth = MethodUtils.invokeMethod(auth, "getSessionAuthorization");
            //get copy of available access groups
            List<String> list = new ArrayList((List<String>) MethodUtils.invokeMethod(sauth, "getAvailableAccessGroups"));
            if (!list.contains(name)) {
                list.add(name);
            }

            MethodUtils.invokeMethod(auth, "setAvailableAccessGroups", list, currentAccessGroup);

            if (isBatch) {
                auth.replaceAccessGroup(tools().getThread(), name);
                result = true;
            } else {
                result = auth.setActiveAccessGroup(tools().getThread(), name);
            }
            if (logger.isInfoEnabled()) {
                logger.info("Switch AG [" + currentAccessGroup + "] -> [" + name + "] succeeded - current: [" + getCurrentAccessGroup() + "]");
            }
        } catch (Exception ex) {
            logger.error("Switch AG [" + currentAccessGroup + "] -> [" + name + "] failed - current: [" + getCurrentAccessGroup() + "]", ex);
        }
        return result;
    }

    private String getCurrentAccessGroup() {
        PegaAuthorization auth = (PegaAuthorization) tools().getAuthorizationHandle();
        String currentAccessGroup = null;
        try {
            currentAccessGroup = (String) MethodUtils.invokeMethod(auth, "getCurrentAccessGroup");
        } catch (Exception ex) {
            logger.error("Failed to get current AG", ex);
        }
        return currentAccessGroup;
    }
}
