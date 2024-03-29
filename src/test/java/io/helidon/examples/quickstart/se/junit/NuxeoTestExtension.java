/*
 * (C) Copyright 2019 Nuxeo (http://nuxeo.com/).
 * This is unpublished proprietary source code of Nuxeo SA. All rights reserved.
 * Notice of copyright on this source code does not indicate publication.
 *
 * Contributors:
 *     Nuxeo
 *
 */

package io.helidon.examples.quickstart.se.junit;

import io.helidon.examples.quickstart.se.nuxeo.HelidonEnvironment;
import io.helidon.examples.quickstart.se.nuxeo.NuxeoApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.nuxeo.common.Environment;
import org.nuxeo.runtime.api.Framework;

import java.util.Arrays;

public class NuxeoTestExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {
    private static final Logger log = LogManager.getLogger(NuxeoTestExtension.class.getName());

    public static NuxeoApplication APP;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        APP = new NuxeoApplication();
        APP.onStart();

        Deploys deploys = extensionContext.getRequiredTestClass().getAnnotation(Deploys.class);
        if (deploys != null) {
            deployContribution(deploys);
            return;
        }
        deployContribution(extensionContext.getRequiredTestClass().getAnnotation(Deploy.class));
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        APP.onStop();
        APP = null;
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        ((HelidonEnvironment) Environment.getDefault()).reloadProperties();

        Deploys deploys = extensionContext.getRequiredTestMethod().getAnnotation(Deploys.class);
        if (deploys != null) {
            undeployContribution(deploys);
            return;
        }
        undeployContribution(extensionContext.getRequiredTestMethod().getAnnotation(Deploy.class));
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        ConfigProperties cps = extensionContext.getRequiredTestMethod().getAnnotation(ConfigProperties.class);
        if (cps != null) {
            Arrays.stream(cps.value()).forEach(cp -> Framework.getProperties().setProperty(cp.key(), cp.value()));
        } else {
            ConfigProperty cp = extensionContext.getRequiredTestMethod().getAnnotation(ConfigProperty.class);

            if (cp != null) {
                Framework.getProperties().setProperty(cp.key(), cp.value());
            }
        }

        Deploys deploys = extensionContext.getRequiredTestMethod().getAnnotation(Deploys.class);
        if (deploys != null) {
            deployContribution(deploys);
            return;
        }
        deployContribution(extensionContext.getRequiredTestMethod().getAnnotation(Deploy.class));

        // Force reset before each test
        resetComponentManager();
    }

    protected void resetComponentManager() {
        Framework.getRuntime().getComponentManager().refresh();
    }

    protected void deployContribution(Deploys deploys) {
        if (deploys == null) {
            return;
        }
        Arrays.stream(deploys.value()).forEach(this::deployContribution);
    }

    protected void undeployContribution(Deploys deploys) {
        if (deploys == null) {
            return;
        }
        Arrays.stream(deploys.value()).forEach(this::undeployContribution);
    }

    protected void undeployContribution(Deploy deploy) {
        if (deploy == null) {
            return;
        }
        Framework.getRuntime().getContext().undeploy(deploy.value());
    }

    protected void deployContribution(Deploy deploy) {
        if (deploy == null) {
            return;
        }
        Framework.getRuntime().getContext().deploy(deploy.value());
    }
}
