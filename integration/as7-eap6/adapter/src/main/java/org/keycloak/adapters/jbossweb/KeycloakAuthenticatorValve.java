package org.keycloak.adapters.jbossweb;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.realm.GenericPrincipal;
import org.keycloak.adapters.tomcat.AbstractKeycloakAuthenticatorValve;
import org.keycloak.adapters.tomcat.GenericPrincipalFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

/**
 * Keycloak authentication valve
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class KeycloakAuthenticatorValve extends AbstractKeycloakAuthenticatorValve {
    public boolean authenticate(Request request, HttpServletResponse response, LoginConfig config) throws java.io.IOException {
        return authenticateInternal(request, response, config);
    }

    @Override
    protected boolean forwardToErrorPageInternal(Request request, HttpServletResponse response, Object loginConfig) throws IOException {
        if (loginConfig == null) return false;
        LoginConfig config = (LoginConfig)loginConfig;
        if (config.getErrorPage() == null) return false;
        forwardToErrorPage(request, (Response)response, config);
        return true;
    }


    @Override
    public void start() throws LifecycleException {
        StandardContext standardContext = (StandardContext) context;
        standardContext.addLifecycleListener(this);
        super.start();
    }


    public void logout(Request request) {
        logoutInternal(request);
    }

    @Override
    protected GenericPrincipalFactory createPrincipalFactory() {
        return new JBossWebPrincipalFactory();
    }
}
