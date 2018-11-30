package ru.sbrf.pegi18.mon.prpc;

import com.pega.pegarules.priv.PegaAPI;
import com.pega.pegarules.priv.authorization.PegaAuthorization;
import com.pega.pegarules.pub.context.ThreadContainer;
import com.pega.pegarules.pub.runtime.ParameterPage;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class AbstractPrpcSource implements PrpcSource {

    protected final Logger logger = LogManager.getLogger(getClass());

//    private PegaAPI tools;

    private String accessGroup;
    private String resultsPropName;
    private String ruleName;
    private String ruleClass;
    private ParameterPage parameterPage;

    PegaAPI tools() {
//        if (tools == null) {
//            tools = (PegaAPI) ThreadContainer.get().getPublicAPI();
//        }
//        return tools;
        return (PegaAPI) ThreadContainer.get().getPublicAPI();
    }

    public String accessGroup() {
        return accessGroup;
    }

    public String resultsPropName() {
        return resultsPropName;
    }

    public String ruleName() {
        return ruleName;
    }

    public String ruleClass() {
        return ruleClass;
    }

    public ParameterPage parameterPage() {
        return parameterPage;
    }

    public abstract static class Builder<T extends AbstractPrpcSource.Builder<T>> {
        private String accessGroup;
        private String resultsPropName;
        private String ruleName;
        private String ruleClass;
        private ParameterPage parameterPage;

        public T accessGroup(String accessGroup) {
            this.accessGroup = accessGroup;
            return self();
        }

        public T resultsPropName(String resultsPropName) {
            this.resultsPropName = resultsPropName;
            return self();
        }

        public T ruleName(String ruleName) {
            this.ruleName = ruleName;
            return self();
        }

        public T ruleClass(String ruleClass) {
            this.ruleClass = ruleClass;
            return self();
        }

        public T parameterPage(ParameterPage parameterPage) {
            this.parameterPage = parameterPage;
            return self();
        }

        @SuppressWarnings("unchecked")
        final T self() {
            return (T) this;
        }

        void build(AbstractPrpcSource source) {
            source.accessGroup = this.accessGroup;
            source.resultsPropName = this.resultsPropName;
            source.ruleName = this.ruleName;
            source.ruleClass = this.ruleClass;
            source.parameterPage = this.parameterPage;
        }
    }

    @SuppressWarnings("unchecked")
    boolean switchAccessGroup(String name) {
        boolean result = false;

        PegaAuthorization auth = (PegaAuthorization) tools().getAuthorizationHandle();
        boolean isBatch = tools().getThread().getRequestor().isBatchType();

        String currentAccessGroup = "";

        try {
            Object sauth = MethodUtils.invokeMethod(auth, "getSessionAuthorization");
            //get copy of available access groups
            List<String> list = new ArrayList((List<String>) MethodUtils.invokeMethod(sauth, "getAvailableAccessGroups"));
            if (!list.contains(name)) {
                list.add(name);
            }

            currentAccessGroup = getCurrentAccessGroup();

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
        } catch (Exception e) {
            logger.error("Switch AG [" + currentAccessGroup + "] -> [" + name + "] failed - current: [" + getCurrentAccessGroup() + "]");
        }
        return result;
    }

    String getCurrentAccessGroup() {
        PegaAuthorization auth = (PegaAuthorization) tools().getAuthorizationHandle();
        String currentAccessGroup = "";
        try {
            currentAccessGroup = (String) MethodUtils.invokeMethod(auth, "getCurrentAccessGroup");
        } catch (Exception ex) {
            logger.error("Failed to get current AG", ex);
        }
        return currentAccessGroup;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(getClass().getSimpleName());
        sb.append("[").append(ruleClass).append("]");
        sb.append("[").append(ruleName).append("]");
        sb.append("[").append(accessGroup).append("]");
        sb.append("[").append(resultsPropName).append("]");
        return sb.toString();
    }
}
