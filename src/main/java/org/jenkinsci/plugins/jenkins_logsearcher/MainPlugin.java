package org.jenkinsci.plugins.jenkins_logsearcher;

import hudson.ExtensionList;
import hudson.Plugin;
import hudson.model.TransientViewActionFactory;
import hudson.model.Api;
import jenkins.model.Jenkins;

import org.kohsuke.stapler.export.Exported;

public class MainPlugin extends Plugin {

    public Api getApi() {
        return new Api(this);
    }

    @Exported
    public String getVersion() {
        return Jenkins.getInstance().getPluginManager().getPlugin(MainPlugin.class).getVersion();
    }

    @Override
    public void start() throws Exception {
        super.start();
        load();
        ExtensionList<TransientViewActionFactory> extensionList = Jenkins.getInstance().getExtensionList(
                TransientViewActionFactory.class);
        boolean inList = false;
        for (TransientViewActionFactory fa : extensionList) {
            if (fa instanceof PluginFactory) {
                inList = true;
                break;
            }
        }

        if (!inList) {
            Jenkins.getInstance().getExtensionList(TransientViewActionFactory.class).add(0, new PluginFactory());
        }
        super.start();
    }
}
