/*
 * (C) Copyright 2019 Nuxeo (http://nuxeo.com/).
 * This is unpublished proprietary source code of Nuxeo SA. All rights reserved.
 * Notice of copyright on this source code does not indicate publication.
 *
 * Contributors:
 *     Nuxeo
 *
 */

package io.helidon.examples.quickstart.se;

import io.helidon.examples.quickstart.se.junit.ConfigProperty;
import io.helidon.examples.quickstart.se.junit.Deploy;
import io.helidon.examples.quickstart.se.junit.NuxeoHelidonTest;
import io.helidon.examples.quickstart.se.junit.NuxeoTestExtension;
import io.helidon.examples.quickstart.se.nuxeo.HelidonEnvironment;
import io.helidon.examples.quickstart.se.nuxeo.HelidonProperties;
import org.junit.jupiter.api.Test;
import org.nuxeo.common.Environment;
import org.nuxeo.runtime.RuntimeService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.kafka.KafkaConfigService;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@NuxeoHelidonTest
public class EmbeddedNuxeoRuntimeTest {

    @Test
    public void shouldResolveEnvVariableAsProperties() {
        Environment defEnv = Environment.getDefault();
        assertThat(defEnv).isInstanceOf(HelidonEnvironment.class);
        assertThat(defEnv.getProperty("lc.ctype")).isNotNull();
    }

    @Test
    @Deploy("test-contrib.xml")
    public void shouldDeployBundleWithAnnotation() {
        KafkaConfigService service = Framework.getService(KafkaConfigService.class);

        Properties defaultConfig = service.getConsumerProperties("default");
        assertThat(defaultConfig.getProperty("max.poll.records")).isEqualTo("1337");
        assertThat(defaultConfig.getProperty("session.timeout.ms")).isEqualTo("${nuxeo.test.kafka.session.timeout.ms}");
    }

    @Test
    @Deploy("test-contrib.xml")
    @ConfigProperty(key = "nuxeo.test.kafka.servers", value = "localhost")
    public void shouldExpandConfig() {
        KafkaConfigService service = Framework.getService(KafkaConfigService.class);

        assertThat(Framework.getProperty("nuxeo.test.kafka.servers")).isEqualTo("localhost");

        assertThat(Framework.getProperties()).isInstanceOf(HelidonProperties.class);

        Properties defaultConfig = service.getConsumerProperties("default");
        assertThat(defaultConfig.getProperty("bootstrap.servers")).isEqualTo("localhost-titi");
    }

    @Test
    public void testNuxeoDirs() {
        assertThat(Environment.getDefault().getLog()).hasName("log");
    }

    @Test
    public void testInstallComponent() throws Exception {
        RuntimeService runtime = NuxeoTestExtension.APP.getRuntime();
        assertThat(runtime.getComponent("org.nuxeo.runtime.stream.kafka.service.test")).isNull();

        NuxeoTestExtension.APP.installComponents("test-contrib.xml");
        assertThat(runtime.getComponent("org.nuxeo.runtime.stream.kafka.service.test")).isNotNull();
    }

    @Test
    public void testMissingComponent() throws Exception {
        try {
            NuxeoTestExtension.APP.installComponents("missing-test-contrib.xml");
            fail("Should have raised an exception for missing contrib!");
        } catch (IllegalArgumentException iea) {
            assertThat(iea.getMessage()).contains("missing-test-contrib.xml");
        }

    }
}
