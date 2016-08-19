package org.graylog;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * Implement the PluginMetaData interface here.
 */
public class NexmoAlarmCallbackMetaData implements PluginMetaData {
    @Override
    public String getUniqueId() {
        return "org.graylog.NexmoAlarmCallbackPlugin";
    }

    @Override
    public String getName() {
        return "NexmoAlarmCallback";
    }

    @Override
    public String getAuthor() {
        return "Christiaan van Tienhoven";
    }

    @Override
    public URI getURL() {
        // TODO Insert correct plugin website
        return URI.create("https://www.graylog.org/");
    }

    @Override
    public Version getVersion() {
        return new Version(1, 0, 0);
    }

    @Override
    public String getDescription() {
        return "Alarm callback for calling the Nexmo SMS API.";
    }

    @Override
    public Version getRequiredVersion() {
        return new Version(2, 0, 0);
    }

    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities() {
        return Collections.emptySet();
    }
}
