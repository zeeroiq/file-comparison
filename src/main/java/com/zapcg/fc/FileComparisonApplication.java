package com.zapcg.fc;

import com.zapcg.fc.file.FileOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@Slf4j
@SpringBootApplication
public class FileComparisonApplication {


	private static final String SOURCE = "D:\\file-comparison\\dat-files\\tmp_1";
	private static final String TARGET = "D:\\file-comparison\\dat-files\\tmp_2";

	public static void main(String[] args) throws IOException {
		SpringApplication.run(FileComparisonApplication.class, args);

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
