import java.util.UUID
import java.util.Scanner
import AES.{decrypt, encrypt}
import scala.collection.mutable.ArrayBuffer
import scala.reflect.io.{File, Path}

object CsvCryptoDemo extends App{

  val dirWithData = "testData"
  val fileWithPlainTextData = File(s"$dirWithData/PlainTextData.csv")
  val fileWithEncryptedData = File(s"$dirWithData/EncryptedData.csv")
  val fileWithDecryptedData = File(s"$dirWithData/DecryptedData.csv")

  Path(dirWithData).createDirectory(failIfExists=false)
  if(!fileWithPlainTextData.exists)
    fileWithPlainTextData.writeAll(s"""Field11,  Field21,   Field31,   Field41
                                    |Field12,         ,   Field32,   Field42
                                    |Field13,  Field23,   Field33,   Field43
                                    |Field14,  Field24,          ,   Field44
                                    |       ,  Field25,   Field35,   Field45""".stripMargin)
  //writeAll closes the file

  def newSalt = UUID.randomUUID().toString

  //helper to make sure resources like files or scanners that must be closed get closed
  def using[A <: { def close(): Unit }, B](resource: A)(f: A => B): B =
    try {
      f(resource)
    } finally {
      resource.close()
    }

  // read and encrypt a line of plain text data generating salt as the first field
  def encryptLine(plainLine: String): String = {
    val plainRow = plainLine.split(",").map(_.trim).toList
    val rowSalt = newSalt
    (rowSalt :: plainRow.map(encrypt(_, rowSalt))).mkString(",")
  }

  //read plain text file and write the encrypted file
  val plainTextScanner = new Scanner(fileWithPlainTextData.bufferedReader())
  using(plainTextScanner)(scanner => {
                              var encryptedLines = ArrayBuffer[String]()
                              while(scanner.hasNext())
                                encryptedLines += encryptLine(scanner.nextLine())
                              fileWithEncryptedData.writeAll(encryptedLines.mkString("\n"))
                            })

  // read and decrypt a line of encrypted text data with salt as the first field
  def decryptLine(plainLine: String): String = {
    val encryptedRow = plainLine.split(",").map(_.trim).toList
    val rowSalt = encryptedRow(0)
    (encryptedRow.drop(1).map(decrypt(_, rowSalt))).mkString(",")
  }

  //read the encrypted file and write the decrypted file
  val encryptedScanner = new Scanner(fileWithEncryptedData.bufferedReader())
  using(encryptedScanner)(scanner => {
                              var decryptedLines = ArrayBuffer[String]()
                              while(scanner.hasNext())
                                decryptedLines += decryptLine(scanner.nextLine())
                              fileWithDecryptedData.writeAll(decryptedLines.mkString("\n"))
                            })
}
