/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetcrawler;

import java.io.File;

/**
 *
 * @author ayhun
 */
public class TweetCrawler {
    // where the data files reside (.bz2 files)
    public static final String dataFolderName = "tweets";
    // keyword that makes a tweet candidate
    public static final String productName = "iphone";
    // keywords that are searched in the candidate tweet. if exists, the tweet will be added to the respective output file
    public static final String[] keywords = new String[]{"battery", "screen", "camera"};
    // number of files that are already processed and total number of files
    public static int numFiles, numFilesProcessed;
    // number of threads to be created
    public static final int numWorkers = 8;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Counting number of files to be processed...");
        numFiles = getFilesCount(new File(dataFolderName));
        numFilesProcessed = 0;
        System.out.println("Number of bz2 files in folder:" + numFiles);
        
        clearOldOutputs();
        Worker[] workers = new Worker[numWorkers];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker(i);
            workers[i].start();
        }

        for (int i = 0; i < workers.length; i++) {
            try {
                workers[i].join();
            } catch (InterruptedException ex) {
                System.out.println("Throwed interrupted exception: " + ex.getMessage());
            }
        }

    }
    
    public static void clearOldOutputs() {
        for(String kw:keywords){
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

}
