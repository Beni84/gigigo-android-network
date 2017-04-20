package com.gigigo.ggglib.network.retrofit;

import com.gigigo.ggglib.network.client.NetworkClient;
import com.gigigo.ggglib.network.executors.NetworkExecutor;
import com.gigigo.ggglib.network.retrofit.client.RetrofitNetworkClient;
import com.gigigo.ggglib.network.retrofit.client.RetrofitNetworkClientBuilder;
import com.gigigo.ggglib.network.retrofit.context.BaseApiClient;
import com.gigigo.ggglib.network.retrofit.context.responses.ApiDataTestMock;
import com.gigigo.ggglib.network.retrofit.context.responses.ApiGenericExceptionResponse;
import com.gigigo.ggglib.network.retrofit.context.responses.ApiGenericResponse;
import com.gigigo.ggglib.network.retrofit.context.responses.ApiResponseMock;
import com.gigigo.ggglib.network.retrofit.context.responses.GitHubResponse;
import com.gigigo.ggglib.network.retrofit.context.responses.HttpResponse;
import com.gigigo.ggglib.network.retrofit.context.responses.utils.ResponseUtils;
import com.gigigo.ggglib.network.retrofit.executors.RetrofitNetworkExecutorBuilder;
import com.gigigo.ggglib.network.retry.NoExceptionRetryOnErrorPolicyImpl;
import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class NetworkClientBuilderExecutorTest {

  private final static int ERROR_RESPONSE_CODE = 500;

  private MockWebServer server;

  @Before public void setUp() throws Exception {
    this.server = new MockWebServer();
    server.setDispatcher(getMockwebserverDispatcherInstance());
  }

  @Test public void apiServiceOKExecutorTest() throws Exception {
    NetworkClient networkClient =
        new RetrofitNetworkClientBuilder(server.url("/").toString(), BaseApiClient.class).build();

    NetworkExecutor networkExecutor =
        new RetrofitNetworkExecutorBuilder(networkClient, GitHubResponse.class).build();

    BaseApiClient apiClient =
        (BaseApiClient) ((RetrofitNetworkClient) networkClient).getApiClient();

    ApiGenericResponse response =
        networkExecutor.executeNetworkServiceConnection(ApiResponseMock.class,
            apiClient.testHttpConnection("ok"));

    ApiDataTestMock testResponse = (ApiDataTestMock) response.getResult();

    assertEquals(testResponse.getTest(), "Hello World");
    assertEquals(response.getHttpResponse().getHttpStatus(), 200);
  }

  /*
  @Test public void apiServiceErrorExecutorTest() throws Exception {
    NetworkClient networkClient =
        new RetrofitNetworkClientBuilder(server.url("/").toString(), BaseApiClient.class).build();

    NetworkExecutor networkExecutor =
        new RetrofitNetworkExecutorBuilder(networkClient, ApiResponseMock.class).build();

    BaseApiClient apiClient =
        (BaseApiClient) ((RetrofitNetworkClient) networkClient).getApiClient();

    ApiGenericResponse response =
        networkExecutor.executeNetworkServiceConnection(ApiErrorResponseMock.class,
            apiClient.testHttpConnection("error"));

    ApiErrorResponseMock testResponse = (ApiErrorResponseMock) response.getBusinessError();

    assertEquals(testResponse.getMessage(), "Error.");
    assertEquals(response.getHttpResponse().getHttpStatus(), ERROR_RESPONSE_CODE);
  }
  */

  @Test(expected = Exception.class) public void apiServiceBadExecutorExceptionTest()
      throws Exception {

    NetworkClient networkClient =
        new RetrofitNetworkClientBuilder(server.url("/").toString(), BaseApiClient.class).build();

    NetworkExecutor networkExecutor =
        new RetrofitNetworkExecutorBuilder(networkClient, ApiResponseMock.class).build();

    BaseApiClient apiClient =
        (BaseApiClient) ((RetrofitNetworkClient) networkClient).getApiClient();

    networkExecutor.executeNetworkServiceConnection(ApiResponseMock.class,
        apiClient.testHttpConnection("bad"));
  }

  @Test public void apiServiceBadExecutorExceptionErrorBusinessTest() throws Exception {

    NetworkClient networkClient =
        new RetrofitNetworkClientBuilder(server.url("/").toString(), BaseApiClient.class).build();

    NetworkExecutor networkExecutor =
        new RetrofitNetworkExecutorBuilder(networkClient, ApiResponseMock.class).retryOnErrorPolicy(
            new NoExceptionRetryOnErrorPolicyImpl()).build();

    BaseApiClient apiClient =
        (BaseApiClient) ((RetrofitNetworkClient) networkClient).getApiClient();

    ApiGenericResponse response =
        networkExecutor.executeNetworkServiceConnection(ApiResponseMock.class,
            apiClient.testHttpConnection("bad"));

    Exception exception = (Exception) response.getBusinessError();
    HttpResponse httpResponse = response.getHttpResponse();
    assertNotNull(exception);
    assertEquals(httpResponse.getHttpStatus(), ApiGenericExceptionResponse.HTTP_EXCEPTION_CODE);
  }

  @After public void tearDown() throws Exception {
    server.shutdown();
  }

  private Dispatcher getMockwebserverDispatcherInstance() {
    return new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        try {

          if (request.getPath().equals("/ok/")) {
            return new MockResponse().setResponseCode(200)
                .setBody(ResponseUtils.getContentFromFile("testOkHttp.json", this));
          } else if (request.getPath().equals("/error/")) {
            return new MockResponse().setStatus("HTTP/1.1 500 KO")
                .setBody(ResponseUtils.getContentFromFile("testErrorHttp.json", this));
          } else {
            return new MockResponse().setResponseCode(ERROR_RESPONSE_CODE)
                .setBody(ResponseUtils.getContentFromFile("testBadHttp.json", this));
          }
        } catch (Exception e) {
          e.printStackTrace();
          return new MockResponse().setResponseCode(ERROR_RESPONSE_CODE).setBody("");
        }
      }
    };
  }
}