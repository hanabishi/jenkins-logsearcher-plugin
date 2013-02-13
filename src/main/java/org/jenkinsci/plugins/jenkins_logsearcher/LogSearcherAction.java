package org.jenkinsci.plugins.jenkins_logsearcher;

import hudson.model.Action;
import hudson.model.User;
import hudson.model.View;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import jenkins.model.Jenkins;

import org.jenkinsci.plugins.jenkins_logsearcher.Collections.SearchResult;
import org.jenkinsci.plugins.jenkins_logsearcher.Collections.SearchSummary;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.bind.JavaScriptMethod;

public class LogSearcherAction implements Action {

    private HashMap<String, List<SearchSummary>> searches = new HashMap<String, List<SearchSummary>>();

    public LogSearcherAction() {
    }

    public String getIconFileName() {
        return "/plugin/jenkins-logsearcher/24x24/SearchIcon.png";
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
            data = data.concat("<tr onClick=\"loadOldSearch('" + getMyList().get(i).getId()
                    + "')\" onmouseover=\"setMousePointer(this)\"><td><img src=\"" + Jenkins.getInstance().getRootUrl()
                    + "/plugin/jenkins-logsearcher/16x16/icon_log.png\"/><b> " + getMyList().get(i).getSearchPattern()
                    + "</b> in <b>" + getMyList().get(i).getProjectPattern() + "</b></td></tr>");
        }
        data = data.concat("</table>");
        return data;
    }

    @JavaScriptMethod
    public String getOldSearch(String id) {
        long longID = Long.parseLong(id);
        for (SearchSummary result : getMyList()) {
            if (longID == result.getId()) {
                return pageGenerator(result);
            }
        }
        return "Unable to find a search with the ID: " + id;
    }

    @JavaScriptMethod
    public String search(String branchPattern, String messagePattern, String maxBuilds, boolean onlyLastBuilds,
            boolean onlyBrokenBuilds) throws IOException {
        Searcher s = new Searcher(branchPattern, messagePattern, onlyBrokenBuilds, onlyLastBuilds,
                Long.parseLong(maxBuilds));
        SearchSummary sum = s.search();

        synchronized (this) {
            if (getMyList().size() >= 10) {
                getMyList().remove(0);
            }
            getMyList().add(sum);
            Jenkins.getInstance().save();
        }
        return pageGenerator(sum);
    }

    public String pageGenerator(SearchSummary sum) {
        String block = "";
        block += "<table><tr><td colspan=\"2\"><b>Search statistics:</b><br />";

        block += "The search using project pattern <b>" + sum.getProjectPattern() + "</b> and message pattern <b>"
                + sum.getSearchPattern() + "</b> returned <b>" + sum.getTotalRowHits() + "</b> rows in <b>"
                + sum.getProjectHits() + "</b> projects and <b>" + sum.getTotalBuildHits()
                + "</b> build. The search took <b>" + sum.getElapsedTime() / 1000 + "</b> seconds to run</td></tr>";

        for (SearchResult result : sum.getSearchResult()) {
            block = block.concat("<tr><td colspan=\"2\">" + result.getLink() + "</td></tr>");
            block = block.concat("<tr><td width=\"20\"></td><td><div id=\"" + result.getID() + "\">"
                    + result.getMessages() + "</div></td></tr>");
        }

        return block + "</table>";
    }

    public HashMap<String, List<SearchSummary>> getSearches() {
        return searches;
    }

    public void setSearches(HashMap<String, List<SearchSummary>> searches) {
        this.searches = searches;
    }

}
