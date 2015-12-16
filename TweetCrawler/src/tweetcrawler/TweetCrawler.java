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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final String dataFolderName = "tweets";
        final String productName = "iphone";
        final String[] keywords = new String[]{"battery", "screen", "camera"};
        
        System.out.println("Number of files in folder:" + getFilesCount(new File(dataFolderName)));
        System.out.println(new File(dataFolderName).listFiles()[1].getAbsolutePath());
    }

    public static int getFilesCount(File file) {
        File[] files = file.listFiles();
        int count = 0;
        for (File f : files) {
            if (f.isDirectory()) {
                count += getFilesCount(f);
            } else if(f.getName().endsWith("bz2")) {
                count++;
            }
        }

        return count;
    }

}
