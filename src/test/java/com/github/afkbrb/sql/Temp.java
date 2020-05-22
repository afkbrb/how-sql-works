package com.github.afkbrb.sql;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Temp {

    @Test
    public void test1() throws IOException {
        CSVParser parser = CSVParser.parse(new FileReader("D:\\Java\\Projects\\how-sql-works\\student.csv"), CSVFormat.DEFAULT);
        for (CSVRecord record : parser) {
            System.out.println(record);
        }
    }

    @Test
    public void test2() {
        System.out.println(Integer.parseInt("6"));
    }
}
