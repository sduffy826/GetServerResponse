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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

public class HttpClientCheckServer {
  private static final String PROPERTYFILE = "server.properties";

  private static String url_secured_by_basic_authentication;
  private static String userId;
  private static String password;

  private static boolean debugIt;

  private static Instant startInstant = null;

  // Constructor - init variables
  public HttpClientCheckServer() {
    Properties properties = PropertyHelper.getProperties(PROPERTYFILE);
    url_secured_by_basic_authentication = properties.getProperty("testUrl");
    userId = properties.getProperty("testUser");
    password = properties.getProperty("testPW");

    debugIt = Boolean.parseBoolean(properties.getProperty("testUrl", "false"));
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
    int returnCode;
    if (debugIt) System.out.println("This is a test");

    HttpClientCheckServer me = new HttpClientCheckServer();

    if (debugIt) System.out.println("Before call");
    me.getElapsedTimeInMilliseconds(true);

    returnCode = 98;
    try {
      returnCode = (me.postRequestWithBasicAuthentication()) ? 0 : 99;
    } catch (IOException e) {
      e.printStackTrace();
    }
    float secs = (float) ((float) me.getElapsedTimeInMilliseconds(false) / 1000.0);
    
    System.out.println("Elapsed time: " + secs);
    if (debugIt) System.out.println("After call");
    System.exit(returnCode);
  }

  // -----------------------------------------------------------------------------
  // Post request to the server
  // -----------------------------------------------------------------------------
  public final boolean postRequestWithBasicAuthentication() throws IOException {
    boolean gotGoodResult = false;
    
    CloseableHttpClient client = HttpClientBuilder.create()
        .setDefaultCredentialsProvider(provider()).build();
    if (debugIt)
      System.out.println("url: " + url_secured_by_basic_authentication);

    HttpPost httpPost = new HttpPost(url_secured_by_basic_authentication);

    String jsonBody = "{ \"groupCode\": \"0000004245\" }";

    StringEntity entity = new StringEntity(jsonBody);

    httpPost.setEntity(entity);
    httpPost.setHeader("Accept", "application/json");
    httpPost.setHeader("Content-type", "application/json");

    CloseableHttpResponse response = client.execute(httpPost);

    if (debugIt)
      System.out.println(
          "Response status code: " + response.getStatusLine().getStatusCode()
              + " response.toString(): " + response.toString());

    BufferedReader reader = new BufferedReader(
        new InputStreamReader(response.getEntity().getContent()));

    StringBuffer result = new StringBuffer();
    String line = "";
    while ((line = reader.readLine()) != null) {
      System.out.println(line);
      result.append(line);
      gotGoodResult = true;
    }
    client.close();
    return gotGoodResult;
  }

  // --------------------------------------------------------------------------------------
  // Return a credentials provider
  // --------------------------------------------------------------------------------------
  private CredentialsProvider provider() {
    final CredentialsProvider provider = new BasicCredentialsProvider();
    final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
        userId, password);
    provider.setCredentials(AuthScope.ANY, credentials);
    if (debugIt)
      System.out.println("Userid: " + userId + " password: " + password);
    if (debugIt)
      System.out.println("credential provider: " + provider.toString());

    return provider;
  }
}