package org.jenkinsci.plugins.jenkins_logsearcher.Collections;

import hudson.model.BallColor;

import java.util.LinkedList;
import java.util.List;

import jenkins.model.Jenkins;

public class SearchResult {
    private String buildName = "";
    private String buildVersion = "";
    private BallColor icon = null;
    private List<String> data = new LinkedList<String>();
    private long ID = 0;

    public long getID() {
        return ID;
    }

    public String getLink() {
        return "<img src=\"" + Jenkins.getInstance().getRootUrl() + "/images/16x16/" + getIcon().getImage()
                + "\"/> <a href=\"" + Jenkins.getInstance().getRootUrl() + "/job/" + getBuildName() + "/"
                + getBuildVersion() + "\">" + getBuildName() + " (#" + getBuildVersion() + ")</a>";
    }

    public String getMessages() {
        String message = "";
        for (String d : getData()) {
            if (!d.trim().isEmpty()) {
                message = message.concat("<b>*</b> " + d.trim() + "<br/>");
            }
        }
        return message;
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public void setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }

    public BallColor getIcon() {
        return icon;
    }

    public void setIcon(BallColor icon) {
        this.icon = icon;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public void setID(long iD) {
        ID = iD;
    }
}