package com.zapcg.fc;

import com.zapcg.fc.file.FileOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@SpringBootApplication
public class FileComparisionApplication {


	private static final String SOURCE = "D:\\file-comparision\\dat-files\\tmp_1";
	private static final String TARGET = "D:\\file-comparision\\dat-files\\tmp_2";

	public static void main(String[] args) throws IOException {
		SpringApplication.run(FileComparisionApplication.class, args);

		FileOperations fileOperations = new FileOperations(SOURCE, TARGET);
		log.info("Processing files ...");
		// fileOperations.processFiles();
		// initially move all the contents source and target to UNPROCESSED
		fileOperations.moveToUnprocessed(SOURCE, TARGET);
		fileOperations.contentEquals(SOURCE, TARGET);
		// take files from UNPROCESSED and check for the contents
		log.info("File processing done.");
	}

}
