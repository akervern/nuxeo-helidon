package io.helidon.examples.quickstart.se.nuxeo;

import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import org.nuxeo.common.codec.Crypto;
import org.nuxeo.common.codec.CryptoProperties;

public class HelidonProperties extends CryptoProperties {

    protected Config config;

    @Override
    public String getProperty(String key) {
        Config configValue = getConfig().get(key);
        if (configValue.exists()) {
            String value = configValue.asString().orElse(null);
            if (Crypto.isEncrypted(value)) {
                return new String(getCrypto().decrypt(value));
            }
            return value;
        }

        return super.getProperty(key);
    }

    private Config getConfig() {
        if (config == null) {
            config = Config.create(ConfigSources.classpath("application.yaml"));
        }
        return config;
    }
}
