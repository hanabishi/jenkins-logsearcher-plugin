package org.jenkinsci.plugins.jenkins_logsearcher;

import hudson.model.BallColor;
import hudson.model.Result;
import hudson.model.Project;
import hudson.model.Run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jenkins.model.Jenkins;

import org.apache.commons.lang.StringEscapeUtils;
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
    private int maxBuildHits;
    private long currentID = 0;
    private boolean onlySearchLastBuilds;
    private boolean caseInsensitive;

    public Searcher(String projectPattern, String messagePattern, boolean onlySearchFailedBuilds,
            boolean onlySearchLastBuilds, int maxBuildHits, boolean caseInsensitive) {
        this.onlySearchFailedBuilds = onlySearchFailedBuilds;
        this.onlySearchLastBuilds = onlySearchLastBuilds;
        this.maxBuildHits = maxBuildHits;
        this.projectPatternString = projectPattern;
        this.messagePatternString = messagePattern;
        this.caseInsensitive = caseInsensitive;
        int flags = caseInsensitive ? Pattern.CASE_INSENSITIVE : 0;

        this.projectPattern = Pattern.compile(projectPattern, flags);
        this.messagePattern = Pattern.compile(messagePattern, flags);
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
        int rows = 0;
        List<String> results = new LinkedList<String>();
        if (!logFile.exists()) {
            return results;
        }

        List<String> data = readFile(logFile);

        for (String line : data) {
            String modline = StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeXml(line));
            Matcher m = messagePattern.matcher(modline);

            boolean found = m.find();
            if (found) {
                rows++;
                totalRowHits++;
                results.add(modline);
            }
            if (rows >= 20) {
                results.add("<b><font color=\"red\">Row limit exeded</font></b>");
                break;
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
                totalBuildHits++;
                if (buildHits <= maxBuildHits || maxBuildHits == 0) {
                    File rootDir = build.getRootDir();
                    SearchResult sr = new SearchResult();
                    List<String> searchFile = searchFile(build.getLogFile());
                    if (!searchFile.isEmpty()) {
                        // sr.getData().add("<b>Jenkins console</b>");
                        sr.getData().addAll(searchFile);
                    }
                    searchFile.clear();

                    // searchFile = searchFile(new File(rootDir, "build.xml"));
                    // if (!searchFile.isEmpty()) {
                    // sr.getData().add("<b>build.xml</b>");
                    // sr.getData().addAll(searchFile);
                    // }
                    // searchFile.clear();

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
            if (onlySearchLastBuilds) {
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
        int totalResults = 0;
        for (Project project : projects) {
            Matcher matcher = projectPattern.matcher(project.getDisplayName());
            if (matcher.matches()) {
                projectHits++;
                List<SearchResult> result = findInProject(project);
                for(SearchResult sr: result){
                    totalResults+=sr.getData().size();
                }
                
                results.addAll(result);
                if(results.size() > 400){
                    SearchResult searchResult = new SearchResult();
                    searchResult.setBuildName("Max result reached");
                    searchResult.setBuildVersion("400");
                    searchResult.setIcon(BallColor.RED_ANIME);
                    searchResult.setID(400);
                    results.add(0, searchResult);
                    break;
                }
            }
        }
        long elapsedTime = System.currentTimeMillis() - start;

        return new SearchSummary(results, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                messagePatternString, projectPatternString, elapsedTime, totalRowHits, projectHits, totalBuildHits,
                caseInsensitive, onlySearchFailedBuilds, onlySearchLastBuilds, this.maxBuildHits);
    }
}
