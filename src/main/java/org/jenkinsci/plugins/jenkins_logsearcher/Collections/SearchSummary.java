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

    public SearchSummary(List<SearchResult> searchResult, String searchDate, String searchPattern,
            String projectPattern, long elapsedTime, long totalRowHits, long projectHits, long totalBuildHits) {
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
}
