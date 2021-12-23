# File Comparison

Java utility for file comparison for directories source/target

SOURCE
- PROCESSED // if file is processed move it to processed directory
  - MATCHED
  - MISMATCHED
- UNPROCESSED // initially move all the files to unprocessed directory
- NEW // source is not in the target move it to NEW

    
TARGET
- PROCESSED // if file is processed move it to processed directory
  - MATCH
  - MISMATCHED
- UNPROCESSED // initially move all of the files to unprocessed directory

###### Dummy test data
- temp_file_1.dat
- temp_file_2.dat