package org.pega.metrics.prpc.source;

import com.pega.pegarules.data.internal.clipboard.ClipboardPropertyFactory;
import com.pega.pegarules.pub.clipboard.ClipboardPage;
import com.pega.pegarules.pub.clipboard.ClipboardProperty;
import com.pega.pegarules.session.internal.authorization.Authorization;
import com.pega.pegarules.session.internal.authorization.SessionAuthorization;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
        logger.debug(() -> toString() + " collect using executable " + tools());

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

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(ruleClass)
                .append(ruleName)
                .append(accessGroupName)
                .append(resultsPropName)
                .hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) return false;
            if (other == this) return true;
            if (other.getClass() != getClass()) return false;

            AbstractRulePrpcSourceBuilder builder = (AbstractRulePrpcSourceBuilder) other;
            return new EqualsBuilder()
                .appendSuper(super.equals(other))
                .append(ruleClass, builder.ruleClass)
                .append(ruleName, builder.ruleName)
                .append(accessGroupName, builder.accessGroupName)
                .append(resultsPropName, builder.resultsPropName)
                .isEquals();
        }
    }

    protected ClipboardProperty wrap(ClipboardPage page) {
        ClipboardPage wrapper = tools().createPage("@baseclass", null);
        ClipboardProperty prop = ClipboardPropertyFactory.getMostSuitableClipboardObject("Results", 'S', wrapper);
        prop.setValue(page);
        wrapper.removeFromClipboard();
        return prop;
    }

    private boolean switchAccessGroup(String name) {
        boolean result = false;

        Authorization auth = (Authorization) tools().getAuthorizationHandle();
        boolean isBatch = tools().getThread().getRequestor().isBatchType();

        String currentAccessGroup = null;

        try {
            currentAccessGroup = getCurrentAccessGroup();
            String ag = currentAccessGroup;

            SessionAuthorization sauth = auth.getSessionAuthorization();
            //get copy of available access groups
            List<String> list = sauth.getAvailableAccessGroups();
            if (!list.contains(name)) {
                list.add(name);
            }

            auth.setAvailableAccessGroups(list, currentAccessGroup);

            if (isBatch) {
                auth.replaceAccessGroup(tools().getThread(), name);
                result = true;
            } else {
                result = auth.setActiveAccessGroup(tools().getThread(), name);
            }
            logger.info(() -> "Switch AG [" + ag + "] -> [" + name + "] succeeded - current: [" + getCurrentAccessGroup() + "]");

        } catch (Exception ex) {
            logger.error("Switch AG [" + currentAccessGroup + "] -> [" + name + "] failed - current: [" + getCurrentAccessGroup() + "]", ex);
        }
        return result;
    }

    private String getCurrentAccessGroup() {
        Authorization auth = (Authorization) tools().getAuthorizationHandle();
        return auth.getCurrentAccessGroup();
    }

}
