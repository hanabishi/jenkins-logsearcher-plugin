package org.jenkinsci.plugins.jenkins_logsearcher;

import hudson.model.Action;
import hudson.model.User;
import hudson.model.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import jenkins.model.Jenkins;

import org.jenkinsci.plugins.jenkins_logsearcher.Collections.SearchSummary;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.bind.JavaScriptMethod;

public class LogSearcherAction implements Action {

    private HashMap<String, List<SearchSummary>> searches = new HashMap<String, List<SearchSummary>>();
    private ArrayList<Integer> options = new ArrayList<Integer>();
    private int defaultKey = 20;

    public LogSearcherAction() {
        options.add(1);
        options.add(5);
        options.add(10);
        options.add(20);
        options.add(50);
    }

    public String getIconFileName() {
        return "/plugin/jenkins-logsearcher/24x24/SearchIcon.png";
    }

    public String getOptions() {
        String optSection = "";
        for (int key : options) {
            optSection = optSection.concat("<option " + (key == defaultKey ? "selected=\"selected\"" : "") + ">" + key
                    + "</option>");
        }
        return optSection;
    }

    private List<SearchSummary> getMyList() {
        User current = User.current();
        String key = "anon";
        if (current != null) {
            key = current.getId();
        }
        if (!searches.containsKey(key)) {
            searches.put(key, new LinkedList<SearchSummary>());
        }
        return searches.get(key);
    }

    public String getDisplayName() {
        return "Log searcher";
    }

    public String getUrlName() {
        return "jenkinslogsearcher";
    }

    public View getRootView() {
        View view = Stapler.getCurrentRequest().findAncestorObject(View.class);
        return view != null ? view : Jenkins.getInstance().getPrimaryView();
    }

    @JavaScriptMethod
    public String getSearchesTable() {
        String data = "<table><tr><td><b>Previous searches:</b></td></tr>";

        for (int i = getMyList().size() - 1; i >= 0; i--) {
            SearchSummary searchSummary = getMyList().get(i);
            data = data.concat("<tr onClick=\"loadOldSearch('" + searchSummary.getId()
                    + "')\" onmouseover=\"setMousePointer(this)\"><td><img src=\"" + Jenkins.getInstance().getRootUrl()
                    + "/plugin/jenkins-logsearcher/16x16/icon_log.png\"/> <b>" + searchSummary.getSearchDate()
                    + "</b><br /><b>" + searchSummary.getSearchPattern() + "</b> in <b>"
                    + searchSummary.getProjectPattern() + "</b></td></tr>");
        }
        data = data.concat("</table>");
        return data;
    }

    public SearchSummary getOldSearch(String id) {
        long longID = Long.parseLong(id);
        for (SearchSummary result : getMyList()) {
            if (longID == result.getId()) {
                return result;
            }
        }
        return null;
    }

    @JavaScriptMethod
    public String getOldSearchResult(String id) {
        SearchSummary s = getOldSearch(id);
        return s != null ? s.pageGenerator() : "";
    }

    @JavaScriptMethod
    public String getOldSearchProject(String id) {
        SearchSummary s = getOldSearch(id);
        return s != null ? s.getProjectPattern() : "";
    }

    @JavaScriptMethod
    public String getOldSearchMessage(String id) {
        SearchSummary s = getOldSearch(id);
        return s != null ? s.getSearchPattern() : "";
    }

    @JavaScriptMethod
    public int getOldSearchBuilds(String id) {
        SearchSummary s = getOldSearch(id);
        if (s != null) {
            return options.contains(s.getMaxBuilds()) ? options.indexOf(s.getMaxBuilds()) : options.indexOf(defaultKey);
        }
        return 0;
    }

    @JavaScriptMethod
    public boolean getOldSearchBroken(String id) {
        SearchSummary s = getOldSearch(id);
        return s != null ? s.isSearchOnlyBrokenBuilds() : false;
    }

    @JavaScriptMethod
    public boolean getOldSearchLast(String id) {
        SearchSummary s = getOldSearch(id);
        return s != null ? s.isSearchOnlyLastBuild() : false;
    }

    @JavaScriptMethod
    public boolean getOldSearchCase(String id) {
        SearchSummary s = getOldSearch(id);
        return s != null ? s.isCaseInsensitive() : false;
    }

    @JavaScriptMethod
    public String search(String branchPattern, String messagePattern, String maxBuilds, boolean onlyLastBuilds,
            boolean onlyBrokenBuilds, boolean chkCI) throws IOException {
        if (messagePattern.length() < 5) {
            return "You need to enter a longer search pattern, the pattern needs to be at least 5 chars long";
        }
        Searcher s = new Searcher(branchPattern, messagePattern, onlyBrokenBuilds, onlyLastBuilds,
                Integer.parseInt(maxBuilds), chkCI);
        SearchSummary sum = s.search();

        synchronized (this) {
            if (getMyList().size() >= 5) {
                getMyList().remove(0);
            }
            getMyList().add(sum);
        }
        return sum.pageGenerator();
    }

    public HashMap<String, List<SearchSummary>> getSearches() {
        return searches;
    }

    public void setSearches(HashMap<String, List<SearchSummary>> searches) {
        this.searches = searches;
    }

}
