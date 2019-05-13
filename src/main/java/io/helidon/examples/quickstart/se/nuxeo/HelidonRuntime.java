package io.helidon.examples.quickstart.se.nuxeo;

import org.nuxeo.common.Environment;
import org.nuxeo.common.codec.CryptoProperties;
import org.nuxeo.runtime.util.SimpleRuntime;

public class HelidonRuntime extends SimpleRuntime {
    public HelidonRuntime() {
        super();
        this.properties = (CryptoProperties) Environment.getDefault().getProperties();
    }
}
