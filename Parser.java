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
  public static String GetHexCode(char character)
    {
        return Integer.toHexString(character);
    }//end
  private static boolean isLeftBracket(char c) {
    String s = GetHexCode(c);
    String[] values = {"28","5B","7B","AB","2329","2768","3008","FF08","FF3B"};
    for (int i = 0; i<values.length; i++) {
      if (s.equals(values[i].toLowerCase()))
        return true;
    }
    return false;
  }
  private static boolean isRightBracket(char c) {
    String s = GetHexCode(c);
    String[] values = {"29","5D","7D","BB","232A","2769","3009","FF09","FF3D"};
    for (int i = 0; i<values.length; i++) {
      if (s.equals(values[i].toLowerCase()))
        return true;
    }
    return false;
  }
  private static int[] autoDetectEncode (Scanner s) {
    int max = 0;
    int count = 0;
    int streak = 0;
    int streakcount = 0;
    int streaksum = 0;
    boolean hasBrackets = false;
    boolean inBracket = false;
    int encode = 0;
    int spaces = 1;
    while (s.hasNext()) {
      char c = s.next().charAt(0);
      if (!inBracket&&(isChineseCharacter(c)||c==' ')) {
        streak++;
      }
      else if (isChineseCharacter(c)&&inBracket) {
        count++;
      }
      else if (isLeftBracket(c)) { 
        inBracket = true;
        streakcount++;
        streaksum+=streak;  
        streak = 0;
      }
      else if (isRightBracket(c)) {
        inBracket = false;
        if (count!=0) {
          hasBrackets = true;
        }
        if (count>max) {
          max = count;
        }
        count = 0;
      }
      else if (!inBracket) {
        streakcount++;
        streaksum+=streak; 
        streak = 0;
      }
    }
    if (!hasBrackets) {
      encode = 3;
    }
    else if (max>1) {
      encode = 1;
    }
    else {
      encode = 2;
    }
    if (streaksum/(streakcount+0.0)>4) {
      spaces = 0;
    }
    int[] output = {encode,spaces};
    return output;
  }
  public static void main (String[] args) {
    System.setProperty("file.encoding","UTF-8");
    String param = fileToString("ParserParameters.txt");
    Scanner paramScanner = new Scanner(param);
    String[] paramArray = new String[10]; //for safety
    int count = 0;
    boolean fromURL = false;
    String url;
    String file;
    boolean autoEncode = true;
    int encode = 1;
    boolean spaces = true;
    String output = "Output.txt";
    //boolean trad = true;
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
    autoEncode = paramArray[3].toLowerCase().contains("yes");
    encode = Integer.parseInt(paramArray[4]);
    spaces = paramArray[5].toLowerCase().contains("yes");
    output = paramArray[6];
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
    if (autoEncode) {
      Scanner copyScanner = null;
      if (fromURL) {
        source = getUrlSource(url);
        copyScanner = new Scanner (source);
      }
      else {
        try {
          copyScanner = new Scanner (new File(file),"UTF-8");
        } catch (FileNotFoundException e) {
        }
      }
      copyScanner.useDelimiter("");
      int[] params = autoDetectEncode(copyScanner);
      encode = params[0];
      spaces = params[1]==1;
    }
    if (encode==1||encode==2) {
      sourceScanner.useDelimiter("");
      while (sourceScanner.hasNext()) {
        char c = sourceScanner.next().charAt(0);
        if (spaces&&c==' ') {
          continue;
        }
        if (isChineseCharacter(c)&&!inBracket) {
          currentTrad+=c;
        }
        else if (isChineseCharacter(c)) {
          if (encode==2) {
            while (currentSimp.length()<currentTrad.length()-1) {
              currentSimp+=currentTrad.charAt(currentSimp.length());
            }
          }
          currentSimp+=c;
        }
        else if (isLeftBracket(c)) { //brackets are screwy
          inBracket = true;
        }
        else if (isRightBracket(c)) {
          inBracket = false;
        }
        else if (!inBracket) {
          if (!currentTrad.equals("")) {
            if (currentSimp.equals("")) {
              currentSimp = currentTrad;
            }
            try {
              outputWriter.write(currentTrad+"\r\n");
            } catch (IOException e) {
            }
            try {
              outputWriter.write(currentSimp+"\r\n");
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
    else {
      while(sourceScanner.hasNext()) {
        String L = sourceScanner.nextLine();
        String trad = "";
        String simp = "";
        boolean tradOn = true;
        boolean simpOn = false;
        for (int i = 0; i<L.length(); i++) {
          char c = L.charAt(i);
          if (isChineseCharacter(c)&&tradOn) {
            trad=trad+c;
          }
          else if (isChineseCharacter(c)&&simpOn) {
            simp=simp+c;
          }
          else {
            if (tradOn&&!trad.equals("")) {
              tradOn=false;
              simpOn=true;
            }
            if (simpOn&&!simp.equals("")) {
              simpOn=false;
            }
          }
          if (!simpOn&&!tradOn) {
            break;
          }
        }
        if (!trad.equals("")) {
            if (simp.equals("")) {
              simp = trad;
            }
            try {
              outputWriter.write(trad+"\r\n");
            } catch (IOException e) {
            }
            try {
              outputWriter.write(simp+"\r\n");
            } catch (IOException e) {
            }
          }
      }
      try {
        outputWriter.close();
      } catch (IOException e) {
      }
    }
  }
}