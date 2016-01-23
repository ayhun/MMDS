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
import org.json.JSONObject;

/**
 *
 * @author ayhun
 */
public class Worker extends Thread {

    // remember which files already processed
    public static final HashMap<String, String> processedFiles = new HashMap<>();
    // id
    private final int id;
    // json fields that are reflected to output
    private final String[] jsonFields = new String[]{"created_at", "timestamp_ms", "id_str", "text", "lang"};

    public Worker(int id) {
        this.id = id;
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
                    JSONObject js = new JSONObject(line);
                    if (js.has("text")) {
                        line = js.getString("text").toLowerCase();
                        if (StringUtils.contains(line, TweetCrawler.productName) && StringUtils.containsAny(line, TweetCrawler.keywords) && !StringUtils.containsAny(line, TweetCrawler.forbiddenWords)) {
                            for (int i = 0; i < TweetCrawler.keywords.length; i++) {
                                if (StringUtils.contains(line, TweetCrawler.keywords[i])) {
                                    TweetCrawler.appendContents(i, stripJSON(js));
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
            } catch (ArrayIndexOutOfBoundsException ex){
                System.out.println("probably corrupted file at " + f.getAbsolutePath() + "Exception:" + ex);
                TweetCrawler.errOut(id, "probably corrupted file at " + f.getAbsolutePath() + "Exception:" + ex.toString());
            } catch (RuntimeException ex) {
                System.out.println("Exception: " + ex.toString());
                TweetCrawler.errOut(id, "Exception:" + ex.toString());
            }
        }
    }

    public BufferedReader getBufferedReaderForCompressedFile(String fileIn) throws FileNotFoundException, CompressorException {
        CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(new BufferedInputStream(new FileInputStream(fileIn)));
        return new BufferedReader(new InputStreamReader(input));
    }

    private String stripJSON(JSONObject json) {
        JSONObject stripped = new JSONObject();
        for (int i = 0; i < jsonFields.length; i++) {
            if(json.has(jsonFields[i]))
                stripped.put(jsonFields[i], json.getString(jsonFields[i]));
        }
        return stripped.toString();
    }
}
