package com.epam.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rauf_Aliev on 8/17/2016.
 */
public class CSVPrint {

    public static void printAsCSV(String result, boolean fix) {
        List<String> lines = Arrays.asList(result.split("\n"));
        List<List<String>> csv = new ArrayList<>();
        for (String line : lines)
        {
            List<String> columns = new ArrayList<>();
            columns.addAll(Arrays.asList(line.split("\t")));
            csv.add(columns);
        }
        CSVPrint.writeCSV(csv, fix);
    }

    public static String writeCSV(List<List<String>> rows, boolean fix) {


        if (rows.size() == 0)
            throw new RuntimeException("No rows");

        // normalize data
        int longest = 0;
        for (List<String> row : rows)
            if (row.size() > longest)
                longest = row.size();

        for (List<String> row : rows)
            while (row.size() < longest)
                row.add("");

        if (longest == 0)
            throw new RuntimeException("No colums");

        // fix special characters
        for (int i = 0; i < rows.size(); i++)
            for (int j = 0; j < rows.get(i).size(); j++)
                rows.get(i).set(j, fix ? fixSpecial(rows.get(i).get(j)) : rows.get(i).get(j));

        // get the maximum size of one column
        int[] maxColumn = new int[rows.get(0).size()];

        for (int i = 0; i < rows.size(); i++)
            for (int j = 0; j < rows.get(i).size(); j++)
                if (maxColumn[j] < rows.get(i).get(j).length())
                    maxColumn[j] = rows.get(i).get(j).length();

        // create the format string
        String outFormat = "";
        for (int max : maxColumn)
            outFormat += "%-" + (max + 1) + "s, ";
        outFormat = outFormat.substring(0, outFormat.length() - 2) + "\n";

        // print the data
        StringBuffer resulting = new StringBuffer();
        for (List<String> row : rows)
            resulting.append(String.format(outFormat, row.toArray()));
        return resulting.toString();

    }

    private static String fixSpecial(String s) {

        s = s.replaceAll("(\")", "$1$1");

        if (s.contains("\n") || s.contains(",") || s.contains("\"") ||
                s.trim().length() < s.length()) {
            s = "\"" + s + "\"";
        }

        return s;
    }
}
