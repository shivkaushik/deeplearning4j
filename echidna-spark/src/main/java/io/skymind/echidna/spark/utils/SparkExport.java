package io.skymind.echidna.spark.utils;

import io.skymind.echidna.spark.misc.WritablesToStringFunction;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.canova.api.writable.Writable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Alex on 7/03/2016.
 */
public class SparkExport {

    //Quick and dirty CSV export (using Spark). Eventually, rework this to use Canova record writers on Spark
    public static void exportCSVSpark(String directory, String delimiter, int outputSplits, JavaRDD<List<Writable>> data) {

        //NOTE: Order is probably not random here...
        JavaRDD<String> lines = data.map(new WritablesToStringFunction(delimiter));
        lines.coalesce(outputSplits);

        lines.saveAsTextFile(directory);
    }

    //Another quick and dirty CSV export (local). Dumps all values into a single file
    public static void exportCSVLocal(File outputFile, String delimiter, JavaRDD<List<Writable>> data, int rngSeed) throws Exception {

        JavaRDD<String> lines = data.map(new WritablesToStringFunction(delimiter));
        List<String> linesList = lines.collect();   //Requires all data in memory
        if(!(linesList instanceof ArrayList)) linesList = new ArrayList<>(linesList);
        Collections.shuffle(linesList, new Random(rngSeed));

        FileUtils.writeLines(outputFile, linesList);
    }

    //Another quick and dirty CSV export (local). Dumps all values into multiple files (specified number of files)
    public static void exportCSVLocal(String outputDir, String baseFileName, int numFiles, String delimiter,
                                      JavaRDD<List<Writable>> data, int rngSeed) throws Exception {

        JavaRDD<String> lines = data.map(new WritablesToStringFunction(delimiter));
        double[] split = new double[numFiles];
        for (int i = 0; i < split.length; i++) split[i] = 1.0 / numFiles;
        JavaRDD<String>[] splitData = lines.randomSplit(split);

        int count = 0;
        Random r = new Random(rngSeed);
        for (JavaRDD<String> subset : splitData) {
            String path = FilenameUtils.concat(outputDir, baseFileName + (count++) + ".csv");
            List<String> linesList = subset.collect();
            if(!(linesList instanceof ArrayList)) linesList = new ArrayList<>(linesList);
            Collections.shuffle(linesList, r);
            FileUtils.writeLines(new File(path), linesList);
        }
    }

    // No shuffling
    public static void exportCSVLocal(String outputDir, String baseFileName, int numFiles, String delimiter,
                                      JavaRDD<List<Writable>> data) throws Exception {

        JavaRDD<String> lines = data.map(new WritablesToStringFunction(delimiter));
        double[] split = new double[numFiles];
        for (int i = 0; i < split.length; i++) split[i] = 1.0 / numFiles;
        JavaRDD<String>[] splitData = lines.randomSplit(split);

        int count = 0;
        for (JavaRDD<String> subset : splitData) {
            String path = FilenameUtils.concat(outputDir, baseFileName + (count++) + ".csv");
//            subset.saveAsTextFile(path);
            List<String> linesList = subset.collect();
            FileUtils.writeLines(new File(path), linesList);
        }
    }

    @AllArgsConstructor
    private static class SequenceToStringFunction implements Function<List<List<Writable>>,String> {

        private final String delim;

        @Override
        public String call(List<List<Writable>> sequence) throws Exception {

            StringBuilder sb = new StringBuilder();
            boolean firstTimeStep = true;
            for(List<Writable> c : sequence ) {
                if(!firstTimeStep) sb.append("\n");
                boolean first = true;
                for (Writable w : c) {
                    if (!first) sb.append(delim);
                    sb.append(w.toString());
                    first = false;
                }
                firstTimeStep = false;
            }

            return sb.toString();
        }
    }



    //Another quick and dirty CSV export (local). Dumps all values into a single file
    public static void exportStringLocal(File outputFile, JavaRDD<String> data, int rngSeed) throws Exception {
        List<String> linesList = data.collect();   //Requires all data in memory
        if(!(linesList instanceof ArrayList)) linesList = new ArrayList<>(linesList);
        Collections.shuffle(linesList, new Random(rngSeed));

        FileUtils.writeLines(outputFile, linesList);
    }

    //Quick and dirty CSV export: one file per sequence, with shuffling of the order of sequences
    public static void exportCSVSequenceLocal(File baseDir, JavaRDD<List<List<Writable>>> sequences, long seed ) throws Exception {
        baseDir.mkdirs();
        if(!baseDir.isDirectory()) throw new IllegalArgumentException("File is not a directory: " + baseDir.toString());
        String baseDirStr = baseDir.toString();

        List<String> fileContents = sequences.map(new SequenceToStringFunction(",")).collect();
        if(!(fileContents instanceof ArrayList)) fileContents = new ArrayList<>(fileContents);
        Collections.shuffle(fileContents,new Random(seed));

        int i=0;
        for(String s : fileContents ){
            String path = FilenameUtils.concat(baseDirStr,i + ".csv");
            File f = new File(path);
            FileUtils.writeStringToFile(f,s);
            i++;
        }
    }

    //Quick and dirty CSV export: one file per sequence, without shuffling
    public static void exportCSVSequenceLocalNoShuffling(File baseDir, JavaRDD<List<List<Writable>>> sequences) throws Exception {
        exportCSVSequenceLocalNoShuffling(baseDir, sequences, "", ",", "csv");
    }
    public static void exportCSVSequenceLocalNoShuffling(File baseDir, JavaRDD<List<List<Writable>>> sequences, String delimiter,
                                                         String filePrefix, String fileExtension) throws Exception {
        baseDir.mkdirs();
        if(!baseDir.isDirectory()) throw new IllegalArgumentException("File is not a directory: " + baseDir.toString());
        String baseDirStr = baseDir.toString();

        List<String> fileContents = sequences.map(new SequenceToStringFunction(delimiter)).collect();
        if(!(fileContents instanceof ArrayList)) fileContents = new ArrayList<>(fileContents);

        int i=0;
        for(String s : fileContents ){
            String path = FilenameUtils.concat(baseDirStr, filePrefix + "_" + i + "." + fileExtension);
            File f = new File(path);
            FileUtils.writeStringToFile(f,s);
            i++;
        }
    }
}
