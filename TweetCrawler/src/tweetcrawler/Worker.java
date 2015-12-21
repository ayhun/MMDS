/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetcrawler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author ayhun
 */
public class Worker extends Thread {

    // remember which files already processed
    public static final HashMap<String, String> processedFiles = new HashMap<>();
    // other vars
    private final String productName;
    private final String[] keywords;
    private final int id;

    public Worker(int id) {
        this.id = id;
        this.productName = TweetCrawler.productName;
        this.keywords = TweetCrawler.keywords;
    }

    @Override
    public void run() {
        workOnFolder(new File(TweetCrawler.dataFolderName));
    }

    private void workOnFolder(File file) {
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                workOnFolder(f);
            } else if (f.getName().endsWith("bz2")) {
                workOnFile(f);
            }
        }
    }

    private void workOnFile(File f) {
        String line;
        boolean proceed = false;
        synchronized (Worker.processedFiles) {
            if (!processedFiles.containsKey(f.getAbsolutePath())) {
                processedFiles.put(f.getAbsolutePath(), "started");
                proceed = true;
            }
        }
        if (proceed) {
            try {
                BufferedReader br = getBufferedReaderForCompressedFile(f.getAbsolutePath());
                while ((line = br.readLine()) != null) {
                    line = StringUtils.replaceEach(line, new String[]{"Twitter for iPhone", "download\\/iphone", "screen_"}, new String[]{"", "", ""}).toLowerCase();
                    if (StringUtils.contains(line, productName)) {
                        if (StringUtils.containsAny(line, keywords)) {
                            for (int i = 0; i < keywords.length; i++) {
                                if (StringUtils.contains(line, keywords[i])) {
                                    TweetCrawler.appendContents(i, line);
                                }
                            }
                        }
                    }
                }

                synchronized (Worker.processedFiles) {
                    processedFiles.put(f.getAbsolutePath(), "done by " + this.id);
                    System.out.println("Thread " + this.id + "\tdid " + f.getPath() + " -- " + 100 * ((double) TweetCrawler.numFilesProcessed++ / TweetCrawler.numFiles) + "% complete");
                }
            } catch (FileNotFoundException ex) {
                System.out.println("File not found: " + f.getAbsolutePath());
            } catch (CompressorException ex) {
                System.out.println("Compressor exception: " + ex.getMessage());
            } catch (IOException ex) {
                System.out.println("Problem reading the file: " + f.getAbsolutePath());
            }
        }
    }

    public BufferedReader getBufferedReaderForCompressedFile(String fileIn) throws FileNotFoundException, CompressorException {
        FileInputStream fin = new FileInputStream(fileIn);
        BufferedInputStream bis = new BufferedInputStream(fin);
        CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
        BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
        return br2;
    }
}
