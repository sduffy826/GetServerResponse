import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;

import com.corti.PropertyHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

public class HttpClientCheckEndPoint {
  private static String url_secured_by_basic_authentication;
  private static String userId;
  private static String password;
  private static String postBody;

  private static boolean debugIt;

  private static Instant startInstant = null;

  // Constructor - init variables
  public HttpClientCheckEndPoint() {
    // Nothing right now :)
  }

  private long getElapsedTimeInMilliseconds(boolean startIt) {
    if (startIt || startInstant == null) {
      startInstant = Instant.now();
      return 0;
    } else {
      Instant ending = Instant.now();
      return Duration.between(startInstant, ending).toMillis();
    }
  }

  // ------------------------------------------------------------------
  // Mainline
  // ------------------------------------------------------------------
  public static void main(String[] args) {
    int httpStatusCode;
    
    if (args.length < 4) {
      System.out.println("Invalid arguments pass: url, userid, passwd, postBodyContent, (optional) debugValue");
      System.exit(999);
    }

    url_secured_by_basic_authentication = args[0];
    userId   = args[1];
    password = args[2];
    postBody = args[3];
    if (args.length > 4 && args[args.length-1].equals("true")) debugIt = true;
    if (debugIt) { for (String arg: args) { System.out.println("arg: " + arg); } }
    
    HttpClientCheckEndPoint me = new HttpClientCheckEndPoint();

    if (debugIt) System.out.println("Before call");
    me.getElapsedTimeInMilliseconds(true);

    httpStatusCode = 998;
    try {
      httpStatusCode = me.postRequestWithBasicAuthentication();      
    } catch (IOException e) {
      e.printStackTrace();
    }
    float secs = (float) ((float) me.getElapsedTimeInMilliseconds(false) / 1000.0);
    
    if (debugIt) System.out.println("Elapsed time: " + secs);
    
    if (httpStatusCode == 200) {
      System.exit(0);  // all good
    }
    else {
      me.sendErrorEmail(url_secured_by_basic_authentication, Integer.toString(httpStatusCode));
      System.exit(4);
    }
  }

  // -----------------------------------------------------------------------------
  // Post request to the server
  // -----------------------------------------------------------------------------
  public int postRequestWithBasicAuthentication() throws IOException {
    int statusCode = 999;
    
    CloseableHttpClient client = HttpClientBuilder.create()
        .setDefaultCredentialsProvider(provider()).build();
    
    if (debugIt) System.out.println("url: " + url_secured_by_basic_authentication);

    HttpPost httpPost = new HttpPost(url_secured_by_basic_authentication);

    // String jsonBody = "{ \"groupCode\": \"0000004245\" }";  Left here just to see format 

    StringEntity entity = new StringEntity(postBody);

    httpPost.setEntity(entity);
    httpPost.setHeader("Accept", "application/json");
    httpPost.setHeader("Content-type", "application/json");

    CloseableHttpResponse response = client.execute(httpPost);

    if (debugIt)
      System.out.println("Response status code: " + response.getStatusLine().getStatusCode()
                       + " response.toString(): " + response.toString());
    
    statusCode = response.getStatusLine().getStatusCode();
      
    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

    StringBuffer result = new StringBuffer();
    String line = "";
    while ((line = reader.readLine()) != null) {
      System.out.println(line);
      result.append(line);
    }
    reader.close();
    client.close();
    return statusCode;
  }

  // --------------------------------------------------------------------------------------
  // Return a credentials provider
  // --------------------------------------------------------------------------------------
  private CredentialsProvider provider() {
    final CredentialsProvider provider = new BasicCredentialsProvider();
    final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
        userId, password);
    
    provider.setCredentials(AuthScope.ANY, credentials);
    
    if (debugIt) System.out.println("Userid: " + userId + " password: " + password);
    if (debugIt) System.out.println("credential provider: " + provider.toString());

    return provider;
  }
  
  private void sendErrorEmail(String _urlInError, String _responseCode) {    
    if (debugIt) PropertyHelper.setDebugValue(true);
    Properties properties = PropertyHelper.getPropertyObject("emailWhenProblem.properties");
    
    String emailPropertyFile = properties.getProperty("emailPropertyFile");
    String targetEmail = properties.getProperty("destEmail","media3@us.ibm.com");
    String subject = "Error with endpoint: " + _urlInError;
    String body = "Status code: " + _responseCode + "\n\nCheck /seanduff/infosphere/checkWebServices" +
                  "\nRun batch job with true parm to see debug info";
        
    SendTextEmail.sendEmail(emailPropertyFile, "", targetEmail, subject, body);
  }
  
}