import java.util.*;
import java.lang.*;
import java.io.*;
import java.nio.file.*;
import java.net.*;
import org.jsoup.*;

public class Parser {
  private static String getUrlSource(String url) {
    try {
      return Jsoup.parse(new URL(url).openStream(), "UTF-8", url).html();
      //return Jsoup.connect(url).get().html();
    } catch (IOException e) {
    }
    return null;
  }
  private static boolean isChineseCharacter (char x) {
    return Character.isIdeographic(x);
  }
  /*
  private static String getUrlSource(String url) {
    try {
      URL myUrl = new URL(url);
     Scanner urlScanner = new Scanner(myUrl.openStream(), "UTF-8");
     StringBuilder sb = new StringBuilder();
     while (urlScanner.hasNextLine()) {
        sb.append(urlScanner.nextLine() + "\n");
     }
     urlScanner.close();
     return sb.toString();
    } catch (IOException e) {
    }
    return null;
  }
  */
  private static String fileToString (String path) {
    {
      try {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded);
      } catch (IOException e) {
      }
      return "";
    }
  }
  public static void main (String[] args) {
    System.setProperty("file.encoding","UTF-8");
    String param = fileToString("ParserParameters.txt");
    Scanner paramScanner = new Scanner(param);
    String[] paramArray = new String[5];
    int count = 0;
    boolean fromURL = false;
    String url;
    String file;
    int encode = 1;
    String output = "Output.txt";
    boolean trad = true;
    while (paramScanner.hasNext()) {
      String line = paramScanner.nextLine();
      String[] lineArray = line.split(": ");
      if (lineArray.length>1) {
        paramArray[count] = lineArray[1];
      }
      else {
        paramArray[count] = "";
      }
      count++;
    }
    fromURL = paramArray[0].toLowerCase().contains("yes");
    url = paramArray[1];
    file = paramArray[2];
    encode = Integer.parseInt(paramArray[3]);
    output = paramArray[4];
    String source;
    Scanner sourceScanner = null;
    if (fromURL) {
      source = getUrlSource(url);
      sourceScanner = new Scanner (source);
    }
    else {
      try {
        sourceScanner = new Scanner (new File(file),"UTF-8");
      } catch (FileNotFoundException e) {
      }
    }
    sourceScanner.useDelimiter("");
    String currentTrad = "";
    String currentSimp = "";
    boolean inBracket = false;
    BufferedWriter outputWriter = null;
    try {
      outputWriter = new BufferedWriter(new OutputStreamWriter(
                     new FileOutputStream(output), "UTF-8"
                     ));
    } catch (IOException e) {
    }
    while (sourceScanner.hasNext()) {
      char c = sourceScanner.next().charAt(0);
      if (isChineseCharacter(c)&&!inBracket) {
        System.out.println("1");
        currentTrad+=c;
      }
      else if (isChineseCharacter(c)) {
        System.out.println("2");
        if (encode==2) {
          while (currentSimp.length()<currentTrad.length()-1) {
            currentSimp+=currentTrad.charAt(currentSimp.length());
          }
        }
        currentSimp+=c;
      }
      else if (c=='(') { //brackets are screwy
        System.out.println("3");
        inBracket = true;
      }
      else if (c==')') {
        System.out.println("4");
        inBracket = false;
      }
      else if (!inBracket) {
        System.out.println("5");
        if (!currentTrad.equals("")) {
          if (currentSimp.equals("")) {
            currentSimp = currentTrad;
          }
          try {
            outputWriter.write(currentTrad+"\n");
          } catch (IOException e) {
          }
          try {
            outputWriter.write(currentSimp+"\n");
          } catch (IOException e) {
          }
          currentTrad = "";
          currentSimp = "";
        }
      }
    }
    try {
      outputWriter.close();
    } catch (IOException e) {
    }
    //System.out.println(html);
  }
}