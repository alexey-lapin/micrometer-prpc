package ru.sbrf.pegi18.mon.prpc;

import com.pega.pegarules.priv.LogHelper;
import com.pega.pegarules.priv.PegaAPI;
import com.pega.pegarules.priv.authorization.PegaAuthorization;
import com.pega.pegarules.pub.context.ThreadContainer;
import io.micrometer.core.instrument.Meter;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class AbstractPrpcSource implements PrpcSource {

//    protected final LogHelper oLog;
//
//    public AbstractPrpcSource() {
//        oLog = new LogHelper(this.getClass());
//    }
//
//    AbstractPrpcSource(LogHelper logHelper) {
//        oLog = logHelper;
//    }

    private PegaAPI tools;

    private String accessGroup;
    private String resultsPropName;
    private String ruleName;
    private String ruleClass;

    PegaAPI tools() {
        if (tools == null) {
            tools = (PegaAPI) ThreadContainer.get().getPublicAPI();
        }
        return tools;
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

    public abstract static class Builder<T extends AbstractPrpcSource.Builder<T>> {
        private String accessGroup;
        private String resultsPropName;
        private String ruleName;
        private String ruleClass;

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

        @SuppressWarnings("unchecked")
        final T self() {
            return (T) this;
        }

        void build(AbstractPrpcSource source) {
            source.accessGroup = this.accessGroup;
            source.resultsPropName = this.resultsPropName;
            source.ruleName = this.ruleName;
            source.ruleClass = this.ruleClass;
        }
    }

    boolean switchAccessGroup(String name) {
        boolean result = false;

        PegaAuthorization auth = (PegaAuthorization) tools().getAuthorizationHandle();
        boolean isBatch = tools.getThread().getRequestor().isBatchType();
//        oLog.debug("Trying to switch to " + name + " access group");

        String currentAccessGroup = "";

        try {
            Object sauth = MethodUtils.invokeMethod(auth, "getSessionAuthorization");
            //get copy of available access groups
            List<String> list = new ArrayList((List<String>) MethodUtils.invokeMethod(sauth, "getAvailableAccessGroups"));
            if (!list.contains(name)) {
                list.add(name);
            }
            currentAccessGroup = (String) MethodUtils.invokeMethod(auth, "getCurrentAccessGroup");
//            oLog.debug("Current AG: " + currentAccessGroup);
            MethodUtils.invokeMethod(auth, "setAvailableAccessGroups", list, currentAccessGroup);

            if (isBatch) {
                auth.replaceAccessGroup(tools.getThread(), name);
                result = true;
            } else {
                result = auth.setActiveAccessGroup(tools.getThread(), name);
            }
//            if (oLog.isDebugEnabled()) {
//                oLog.debug("Switched AG: " + org.apache.commons.lang3.reflect.MethodUtils.invokeMethod(auth, "getCurrentAccessGroup"));
//            }
        } catch (Exception e) {
//            oLog.error("Failed to switch access group [" + currentAccessGroup + "] to [" + name + "]", e);
        }
        return result;
    }

    String getCurrenAccessGroup() {
        PegaAuthorization auth = (PegaAuthorization) tools().getAuthorizationHandle();
        String currentAccessGroup = "";
        try {
            Object sauth = MethodUtils.invokeMethod(auth, "getSessionAuthorization");
            currentAccessGroup = (String) MethodUtils.invokeMethod(auth, "getCurrentAccessGroup");
        } catch (Exception ex) {

        }
        return currentAccessGroup;
    }
}
