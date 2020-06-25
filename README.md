# PIDS - Pattern Inference and Data Extractor

This package contains the inference and extractor component as described in the paper Hao Jiang, Chunwei Liu, Qi Jin, John Paparrizos, Aaron J. Elmore: PIDS: Attribute Decomposition for Improved Compression and Query Performance in Columnar Storage. Proc. VLDB Endow. 13(6): 925-938 (2020) 

http://www.vldb.org/pvldb/vol13/p925-jiang.pdf

## Running PIDS
### Building PIDS
PIDS is built using Maven. Make sure JDK and Maven are installed on the environment, and run the following command in folder `<PIDS_ROOT>` to build jar 
from source code

`mvn clean package`

### Extract Pattern from Input File
PIDS provides an executable to check if a file has an extractable pattern, and print out the pattern if one is found. The following command runs the pattern extractor.
                                                                          
    java -cp <PIDS_ROOT>/main/target/subattr-main-1.0-SNAPSHOT-jar-with-dependencies.jar edu.uchicago.cs.db.subattr.cli.ExtractPattern <input_file> 


### Running Column Extractor
PIDS provides an executable to extract columns from a given input file. The following command
runs the extractor.

    java -cp <PIDS_ROOT>/main/target/subattr-main-1.0-SNAPSHOT-jar-with-dependencies.jar edu.uchicago.cs.db.subattr.cli.ExtractColumn <input_file> <output_file>

The output consists of two files:

* `<output_file>` contains the extracted columns in tsv format,
* `<output_file>.outlier` contains the outliers recognized by PIDS.

### Synthetic Dataset Generator

PIDS provides generators of the 4 synthetic datasets used in the paper. The following command runs the data generator.

    java -cp <PIDS_ROOT>/main/target/subattr-main-1.0-SNAPSHOT-jar-with-dependencies.jar edu.uchicago.cs.db.subattr.datagen.DatasetGen -t <type> -n <num_output>

    usage: DatasetGen
     -n,--num <arg>    Number of Rows
     -t,--type <arg>   Dataset Type(ipv6/timestamp/phone/address)

The output will be generated to the screen. Pipe the output to a file to save it.

## Pattern Language 

### Quick Start
PIDS describes patterns in a simple language. Before diving into grammar details, we use phone number as an example to give the reader a first impression. When running `ExtractPattern` on the synthetic phone dataset, you will get the following output

   `<S>((,<intany 3:3, false>,),<intany 3:3, false>,-,<intany 4:4, false>)`

The `<S>` at the beginning means the following parenthesis contains a sequence of sub-patterns, separated by `,`. The sequence above contains a `(`, an `<intany 3:3, false>`, a `)`, another `<intany 3:3, false>`, a `-`, and a `<intany 4:4, false>`.

The pattern `<intany x:y, hex>` represents an integer sequence with length between `x` and `y`. The value of `-1` means no limitation. If `hex` is true, this number is a hexadecimal integer.

For non-numeric data, we have pattern `<wordany x:y>` and `<labelany x:y>`, in which `x` and `y` still represents the lower bound and upper bound of the pattern length. `labelany` consists of a combination of letter and digits, and `wordany` consists of symbols appearing in the English words, including letter, digit, `.`, `-`, and space.

We now look at a similar pattern

    `<S>((,<intany 3:3, false>,),<intany 1:-1, false>,<U>(-,<empty>),<intany 0:-1, false>)`
    
It contains a subpattern `<U>(-,<empty>)`. The `<U>` means the content in the subsequent parenthesis consist of an union. The pattern above is a union of symbol `-` and `<empty>`. `<empty>` is a special symbol that matches nothing. Thus `<U>(-,<empty>)` matches either `-` or nothing. In other words, an optional `-`.