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
//import org.baeldung.httpclient.ResponseUtil;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

//import static org.hamcrest.Matchers.equalTo;
//import static org.junit.Assert.assertThat;

/*
 * NOTE : Need module spring-security-rest-basic-auth to be running
 */

public class HttpClientAuthLiveTest {
    private static final String PROPERTYFILE = "server.properties";
    
    // Prod/Test connection values
    private static String prodUser;
    private static String prodPW;
    private static String prodUrl;
    
    private static String testUser;
    private static String testPW;
    private static String testUrl;
    
    // Values for connection
    private static String url_secured_by_basic_authentication;     
    private static String userId;
    private static String password;
    
    
    public HttpClientAuthLiveTest() {
      Properties properties = PropertyHelper.getProperties(PROPERTYFILE);
       
      prodUser = properties.getProperty("prodUser");
      prodPW   = properties.getProperty("prodPW");
      prodUrl  = properties.getProperty("prodUrl");
    
      testUser = properties.getProperty("testUser");
      testPW   = properties.getProperty("testPW");
      testUrl  = properties.getProperty("testUrl");
    
      // Values for connection
      url_secured_by_basic_authentication = testUrl;
      userId                              = testUser;
      password                            = testPW;
    }

    private static Instant startInstant = null;
    
    private long getElapsedTimeInMilliseconds(boolean startIt) {
      if (startIt || startInstant == null) {
        startInstant = Instant.now(); 
        return 0;
      }
      else {
        Instant ending = Instant.now();
        return Duration.between(startInstant,  ending).toMillis();
      }
    }
    
    private CloseableHttpClient client;

    private CloseableHttpResponse response;

    public static void main(String[] args) {        
      System.out.println("This is a test");
      HttpClientAuthLiveTest me = new HttpClientAuthLiveTest();
      System.out.println("Before call");
      me.getElapsedTimeInMilliseconds(true);
      try {
        me.whenExecutingBasicPostRequestWithBasicAuthenticationEnabled_thenSuccess();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      float secs = (float) ((float)me.getElapsedTimeInMilliseconds(false) / 1000.0);
      System.out.println("Elapsed time: " + secs);
      System.out.println("After call");
    }
    
    //@Before
    public final void before() {
        client = HttpClientBuilder.create().build();
    }

    //@After
    public final void after() throws IllegalStateException, IOException {
  //      ResponseUtil.closeResponse(response);
    }

    // tests

    //@Test
    //public final void whenExecutingBasicPostRequestWithBasicAuthenticationEnabled_thenSuccess() throws IOException {
    public final void whenExecutingBasicPostRequestWithBasicAuthenticationEnabled_thenSuccess() throws IOException {
        client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider()).build();

        // response = client.execute(new HttpGet(url_secured_by_basic_authentication));
        // System.out.println("ResponseFromGet: " + response.toString());
        // final int statusCode = response.getStatusLine().getStatusCode();
        
        HttpPost httpPost = new HttpPost(url_secured_by_basic_authentication);
        
        String jsonBody = "{ \"groupCode\": \"0000004245\" }";
        StringEntity entity = new StringEntity(jsonBody);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
     
        CloseableHttpResponse response = client.execute(httpPost);
        System.out.println("Response status code: " + response.getStatusLine().getStatusCode());
       
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = reader.readLine()) != null) {
          result.append(line);
        }
        System.out.println(result.toString());
        
        
        // assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
        client.close();
    }
    
    
    //@Test
    public final void whenExecutingBasicGetRequestWithBasicAuthenticationEnabled_thenSuccess(String url) throws IOException {
        client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider()).build();

        response = client.execute(new HttpGet(url));

        final int statusCode = response.getStatusLine().getStatusCode();
        // assertThat(statusCode, equalTo(HttpStatus.SC_OK));
    }

    //@Test
    public final void givenAuthenticationIsPreemptive_whenExecutingBasicGetRequestWithBasicAuthenticationEnabled_thenSuccess() throws IOException {
        client = HttpClientBuilder.create().build();
        response = client.execute(new HttpGet(url_secured_by_basic_authentication), context());

        final int statusCode = response.getStatusLine().getStatusCode();
        // assertThat(statusCode, equalTo(HttpStatus.SC_OK));
    }

    //@Test
    public final void givenAuthorizationHeaderIsSetManually_whenExecutingGetRequest_thenSuccess() throws IOException {
        client = HttpClientBuilder.create().build();

        final HttpGet request = new HttpGet(url_secured_by_basic_authentication);
        request.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeader(userId, password));
        response = client.execute(request);

        final int statusCode = response.getStatusLine().getStatusCode();
        // assertThat(statusCode, equalTo(HttpStatus.SC_OK));
    }

    //@Test
    public final void givenAuthorizationHeaderIsSetManually_whenExecutingGetRequest_thenSuccess2() throws IOException {
        final HttpGet request = new HttpGet(url_secured_by_basic_authentication);
        final String auth = userId + ":" + password;
        final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        final String authHeader = "Basic " + new String(encodedAuth);
        request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

        client = HttpClientBuilder.create().build();
        response = client.execute(request);

        final int statusCode = response.getStatusLine().getStatusCode();
        // assertThat(statusCode, equalTo(HttpStatus.SC_OK));
    }

    // UTILS

    private CredentialsProvider provider() {
        final CredentialsProvider provider = new BasicCredentialsProvider();
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userId, password);
        provider.setCredentials(AuthScope.ANY, credentials);
        System.out.println("Userid: " + userId + " Password: " + password);
        return provider;
    }

    private HttpContext context() {
        final HttpHost targetHost = new HttpHost("localhost", 8080, "http");
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userId, password));

        // Create AuthCache instance
        final AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        authCache.put(targetHost, new BasicScheme());

        // Add AuthCache to the execution context
        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        return context;
    }

    private String authorizationHeader(final String username, final String password) {
        final String auth = username + ":" + password;
        final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));

        return "Basic " + new String(encodedAuth);
    }
 }
