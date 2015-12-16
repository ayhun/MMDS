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

    public static final String dataFolderName = "tweets";
    public static final String productName = "iphone";
    public static final String[] keywords = new String[]{"battery", "screen", "camera"};
    public static int numFiles, numFilesProcessed;
    public static final int numWorkers = 16;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Number of files in folder:" + getFilesCount(new File(dataFolderName)));
        numFiles = getFilesCount(new File(dataFolderName));
        numFilesProcessed = 0;
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
