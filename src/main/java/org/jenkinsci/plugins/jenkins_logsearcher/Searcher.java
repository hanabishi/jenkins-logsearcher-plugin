package org.jenkinsci.plugins.jenkins_logsearcher;

import hudson.model.Result;
import hudson.model.Project;
import hudson.model.Run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jenkins.model.Jenkins;

import org.jenkinsci.plugins.jenkins_logsearcher.Collections.SearchResult;
import org.jenkinsci.plugins.jenkins_logsearcher.Collections.SearchSummary;

public class Searcher {

    private Pattern projectPattern;
    private Pattern messagePattern;
    private String projectPatternString;
    private String messagePatternString;
    private boolean onlySearchFailedBuilds;
    private long projectHits = 0;
    private long totalBuildHits = 0;
    private long totalRowHits = 0;
    private long maxBuildHits;
    private long currentID = 0;
    private boolean searchOnlyLastBuilds;

    public Searcher(String projectPattern, String messagePattern, boolean onlySearchFailedBuilds,
            boolean searchOnlyLastBuilds, long maxBuildHits) {
        this.onlySearchFailedBuilds = onlySearchFailedBuilds;
        this.searchOnlyLastBuilds = searchOnlyLastBuilds;
        this.maxBuildHits = maxBuildHits;
        this.projectPatternString = projectPattern;
        this.messagePatternString = messagePattern;
        this.projectPattern = Pattern.compile(projectPattern);
        this.messagePattern = Pattern.compile(messagePattern);
    }

    public static void tryClose(FileReader stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (Exception e) {
        }
    }

    public static void tryClose(BufferedReader stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (Exception e) {
        }
    }

    private List<String> readFile(File logFile) {
        List<String> data = new LinkedList<String>();
        FileReader fileReader = null;
        BufferedReader reader = null;
        try {
            fileReader = new FileReader(logFile);
            reader = new BufferedReader(fileReader);
            String line;
            while ((line = reader.readLine()) != null) {
                data.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tryClose(fileReader);
            tryClose(reader);
        }

        return data;
    }

    private List<String> searchFile(File logFile) {
        long rowHits = 0;
        List<String> results = new LinkedList<String>();
        if (!logFile.exists()) {
            return results;
        }

        List<String> data = readFile(logFile);

        for (String line : data) {
            Matcher m = messagePattern.matcher(line);
            if (m.matches()) {
                totalRowHits++;
                rowHits++;
                results.add(line.replaceAll(Pattern.quote("\\"), "\\\\"));
            }
        }

        data.clear();
        return results;
    }

    @SuppressWarnings("rawtypes")
    private List<SearchResult> findInProject(Project project) {
        long buildHits = 0;
        LinkedList<SearchResult> result = new LinkedList<SearchResult>();
        Run build = project.getLastCompletedBuild();
        while (build != null) {
            boolean match = build.getResult().isWorseThan(Result.SUCCESS) || !onlySearchFailedBuilds;
            if (match) {
                buildHits++;
                if (buildHits <= maxBuildHits || maxBuildHits == 0) {
                    File rootDir = build.getRootDir();
                    SearchResult sr = new SearchResult();
                    sr.getData().addAll(searchFile(new File(rootDir, "log")));
                    sr.getData().addAll(searchFile(new File(rootDir, "build.xml")));
                    if (!sr.getData().isEmpty()) {
                        sr.setBuildName(project.getDisplayName());
                        sr.setBuildVersion(build.getNumber() + "");
                        sr.setIcon(build.getIconColor());
                        sr.setID(currentID++);
                        result.add(sr);
                    }
                } else {
                    break;
                }
            }
            if (searchOnlyLastBuilds) {
                break;
            }
            build = build.getPreviousBuild();
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    public SearchSummary search() {
        long start = System.currentTimeMillis();
        List<SearchResult> results = new LinkedList<SearchResult>();
        List<Project> projects = Jenkins.getInstance().getProjects();
        if (projects == null) {
            return null;
        }

        for (Project project : projects) {
            Matcher matcher = projectPattern.matcher(project.getDisplayName());
            if (matcher.matches()) {
                projectHits++;
                results.addAll(findInProject(project));
            }
        }
        long elapsedTime = System.currentTimeMillis() - start;
        return new SearchSummary(results, "date", messagePatternString, projectPatternString, elapsedTime,
                totalRowHits, projectHits, totalBuildHits);
    }

}
