import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;

public class ShowSiteCertificates {

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    try {
//      URL destURL = new URL("https://ralbz001027.raleigh.ibm.com:9443/ibm/iis/launchpad/");
      // URL destURL = new URL("https://www.ibm.com");
      URL destURL = new URL("https://ralbz001027.raleigh.ibm.com");
      HttpsURLConnection conn = (HttpsURLConnection) destURL.openConnection();
      conn.connect();
      Certificate[] certs = conn.getServerCertificates();
      System.out.println("Number of certs: " + certs.length);
      for (Certificate cert: certs) {
        System.out.println("\n\n------------------------------------------------------");
        System.out.println("cert: " + cert);
      }
      
      
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    

  }

}
