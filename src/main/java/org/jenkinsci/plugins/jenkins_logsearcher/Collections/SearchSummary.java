package org.jenkinsci.plugins.jenkins_logsearcher.Collections;

import java.util.List;
import java.util.Random;

public class SearchSummary {

    private List<SearchResult> searchResult;
    private String searchDate;
    private long elapsedTime;
    private String searchPattern;
    private String projectPattern;
    private long totalRowHits;
    private long projectHits;
    private long totalBuildHits;
    private long id = 0;
    public static Random rnd = new Random();
    private boolean caseInsensitive;
    private boolean searchOnlyBrokenBuilds;
    private boolean searchOnlyLastBuild;
    private int maxBuilds;

    public SearchSummary(List<SearchResult> searchResult, String searchDate, String searchPattern,
            String projectPattern, long elapsedTime, long totalRowHits, long projectHits, long totalBuildHits,
            boolean caseInsensitive, boolean searchOnlyBrokenBuilds, boolean searchOnlyLastBuild, int maxBuilds) {
        this.setMaxBuilds(maxBuilds);
        this.setCaseInsensitive(caseInsensitive);
        this.setSearchOnlyBrokenBuilds(searchOnlyBrokenBuilds);
        this.setSearchOnlyLastBuild(searchOnlyLastBuild);
        this.setTotalRowHits(totalRowHits);
        this.setProjectHits(projectHits);
        this.setTotalBuildHits(totalBuildHits);
        this.setSearchPattern(searchPattern);
        this.setProjectPattern(projectPattern);
        this.setSearchResult(searchResult);
        this.setSearchDate(searchDate);
        this.setElapsedTime(elapsedTime);
        setId(rnd.nextLong());
    }

    public List<SearchResult> getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(List<SearchResult> searchResult) {
        this.searchResult = searchResult;
    }

    public String getSearchDate() {
        return searchDate;
    }

    public void setSearchDate(String searchDate) {
        this.searchDate = searchDate;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getSearchPattern() {
        return searchPattern;
    }

    public void setSearchPattern(String searchPattern) {
        this.searchPattern = searchPattern;
    }

    public String getProjectPattern() {
        return projectPattern;
    }

    public void setProjectPattern(String projectPattern) {
        this.projectPattern = projectPattern;
    }

    public long getTotalRowHits() {
        return totalRowHits;
    }

    public void setTotalRowHits(long totalRowHits) {
        this.totalRowHits = totalRowHits;
    }

    public long getProjectHits() {
        return projectHits;
    }

    public void setProjectHits(long projectHits) {
        this.projectHits = projectHits;
    }

    public long getTotalBuildHits() {
        return totalBuildHits;
    }

    public void setTotalBuildHits(long totalBuildHits) {
        this.totalBuildHits = totalBuildHits;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public boolean isSearchOnlyBrokenBuilds() {
        return searchOnlyBrokenBuilds;
    }

    public void setSearchOnlyBrokenBuilds(boolean searchOnlyBrokenBuilds) {
        this.searchOnlyBrokenBuilds = searchOnlyBrokenBuilds;
    }

    public boolean isSearchOnlyLastBuild() {
        return searchOnlyLastBuild;
    }

    public void setSearchOnlyLastBuild(boolean searchOnlyLastBuild) {
        this.searchOnlyLastBuild = searchOnlyLastBuild;
    }

    public String pageGenerator() {
        String block = "";
        block += "<table><tr><td colspan=\"2\"><b>Search statistics:</b><br />";

        block += "The search using project pattern <b>" + getProjectPattern() + "</b> and message pattern <b>"
                + getSearchPattern() + "</b> returned <b>" + getTotalRowHits() + "</b> rows in <b>" + getProjectHits()
                + "</b> projects and <b>" + getTotalBuildHits() + "</b> build. The search took <b>" + getElapsedTime()
                / 1000 + "</b> seconds to run</td></tr>";

        for (SearchResult result : getSearchResult()) {
            block = block.concat("<tr><td colspan=\"2\">" + result.getLink() + "</td></tr>");
            block = block.concat("<tr><td width=\"20\"></td><td><div id=\"" + result.getID() + "\">"
                    + result.getMessages() + "</div></td></tr>");
        }

        return block + "</table>";
    }

    public int getMaxBuilds() {
        return maxBuilds;
    }

    public void setMaxBuilds(int maxBuilds) {
        this.maxBuilds = maxBuilds;
    }
}
