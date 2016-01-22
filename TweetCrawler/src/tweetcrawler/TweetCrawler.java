/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetcrawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author ayhun
 */
public class TweetCrawler {

    // where the data files reside (.bz2 files)
    public static final String dataFolderName = "tweets";
    // keyword that makes a tweet candidate (!!!!!!!!!!must be lowercase!!!!!!!!!!)
    public static final String productName = "iphone";
    // keywords that are searched in the candidate tweet. if exists, the tweet will be added to the respective output file (!!!!!!!!!!must be lowercase!!!!!!!!!!)
    public static final String[] keywords = new String[]{"battery", "screen", "camera", "iphone"};// screen has a space after it because we get many false positives like screenshot. I will find a better solution later
    // keywords that causes tweet to be ignored
    public static final String[] forbiddenWords = new String[]{"http"};
    // a bufferedwriter for each keyword. Every bufferedwriter writes to a different file that contains tweets that contain a specific keyword
    public static BufferedWriter[] outFiles;
    // number of files that are already processed and total number of files
    public static int numFiles, numFilesProcessed;
    // number of threads to be created (use number of cpus if 0)
    public static final int numWorkers = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Counting number of files to be processed...");
        numFiles = getFilesCount(new File(dataFolderName));
        numFilesProcessed = 0;
        System.out.println("Number of bz2 files in folder:" + numFiles);
        
        clearOldOutputs();
        createBufferedWriters();
        
        String startTime = new Date().toString();
        Worker[] workers = new Worker[(numWorkers == 0) ? Runtime.getRuntime().availableProcessors():numWorkers];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker(i);
            workers[i].start();
        }

        for (Worker worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException ex) {
                System.out.println("Throwed interrupted exception: " + ex.getMessage());
            }
        }

        closeFiles();
        System.out.println("bye..");
        try {
            FileWriter fw = new FileWriter("finished.txt");
            fw.write(numFiles + " files has been successfully processed.\nstart:" + startTime + "\nfinish:" + new Date().toString());
            fw.close();
        } catch (IOException ex) {
            System.out.println("It should be fine if you made it here anyways");;
        }
    }

    public static void clearOldOutputs() {
        for (String kw : keywords) {
            File f = new File(kw + ".json");
            if (f.exists()) {
                f.delete();
            }
        }
    }

    public static int getFilesCount(File file) {
        File[] files = file.listFiles();
        int count = 0;
        for (File f : files) {
            if (f.isDirectory()) {
                count += getFilesCount(f);
            } else if (f.getName().endsWith("bz2")) {
                count++;
            }
        }

        return count;
    }

    public static void appendContents(int keywordIndex, String content) {
        if (keywordIndex >= outFiles.length || keywordIndex < 0) {
            return;
        }
        synchronized (outFiles[keywordIndex]) {
            try {
                outFiles[keywordIndex].write(content + "\n");
            } catch (IOException oException) {
                System.out.println("Error appending/File cannot be written: \n" + keywords[keywordIndex] + ".json");
            }
        }

    }

    private static void createBufferedWriters() {
        outFiles = new BufferedWriter[keywords.length];
        for (int i = 0; i < keywords.length; i++) {
            try {
                outFiles[i] = new BufferedWriter(new FileWriter(keywords[i] + ".json", true));
            } catch (IOException ex) {
                System.out.println("Error appending/File cannot be written: \n" + keywords[i] + ".json");
            }
        }
    }

    private static void closeFiles() {
        for (BufferedWriter bw : outFiles) {
            try {
                bw.close();
            } catch (IOException ex) {
                System.out.println("There was a problem closing the file:\n" + ex.getMessage());
            }
        }
    }
}
