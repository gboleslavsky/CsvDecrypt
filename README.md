# Background 

A scala command line app that encrypts and decrypts contents of a simple csv file using SBT 1.2.8 and Scala 2.12.8.
Uses Codecs from Play so it has a dependency on Play 2.7.0

A. To run:
- Clone the repository in some local folder:
    git clone https://github.com/gboleslavsky/CsvDecrypt.git

- Navigate to a CsvDecrypt directory created there:
    cd CsvDecrypt

- run it:
    sbt run

    this compiles the two files and runs the app, CsvCryptoDemo.

- Examine contents of a new subdirectory of CsvDecrypt directory the app created, testData.

B. What the app does:

To demonstrate the functionality of decrypting encrypted data:
- the app first creates a folder testData and generates a plain csv sample file called PlainTextData.csv with 5 rows of 4 fields each
- then it generates a file EncryptedData.csv that contains the encrypted data from PlainTextData.csv Each row of the encrypted file
contains a new first field used as salt
- finally it generates a file DecryptedData.csv that contains the data from EncryptedData.csv decrypted using the salt for each row.

To see it work on more and/or different input, simply edit PlainTextData.csv using the same simple csv format and run the app again.
The app will not overwrite PlainTextData.csv if it's already there, and EncryptedData.csv and DecryptedData.csv always reflect encryption/decryption of
the data the app finds in PlainTextData.csv

The app works if the contents of PlainTextData.csv and DecryptedData.csv are identical (except for blank spaces, which get trimmed
before encryption so decrypted empty fields are just empty strings and not original spaces that stood for null fields).

Unit tests were neglected.



