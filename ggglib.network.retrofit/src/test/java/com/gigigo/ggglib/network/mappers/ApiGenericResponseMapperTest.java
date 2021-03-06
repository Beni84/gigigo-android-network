package com.gigigo.ggglib.network.mappers;

import com.gigigo.ggglib.core.business.model.BusinessContentType;
import com.gigigo.ggglib.core.business.model.BusinessObject;
import com.gigigo.ggglib.network.executors.NetworkExecutor;
import com.gigigo.ggglib.network.responses.ApiErrorResponseMock;
import com.gigigo.ggglib.network.responses.ApiResultDataMock;
import com.gigigo.ggglib.network.responses.ApiErrorDataMock;
import com.gigigo.ggglib.network.responses.ApiGenericExceptionResponse;
import com.gigigo.ggglib.network.responses.ApiGenericResponse;
import com.gigigo.ggglib.network.responses.ApiResultResponseMock;
import com.gigigo.ggglib.network.responses.HttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ApiGenericResponseMapperTest {

  NetworkExecutor apiServiceExecutor;
  ApiGenericResponseMapper apiGenericResponseMapper;

  @Before public void setUp() {

    apiServiceExecutor = Mockito.mock(NetworkExecutor.class);
    apiGenericResponseMapper = new BaseApiResponseMapper(new MapperTest());

    when(apiServiceExecutor.call("ok")).thenReturn(mockApiResponseOkClass());

    when(apiServiceExecutor.call("error")).thenReturn(mockApiResponseBusinessErrorClass());

    when(apiServiceExecutor.call("bad")).thenReturn(mockApiResponseExceptionClass());
  }

  @Test public void mapperOkResultTest() throws Exception {
    ApiGenericResponse apiGenericResponse = apiServiceExecutor.call("ok");

    BusinessObject<DataMock> bo =
        apiGenericResponseMapper.mapApiGenericResponseToBusiness(apiGenericResponse);

    assertEquals(bo.getBusinessError().getBusinessContentType(),
        BusinessContentType.NO_ERROR_CONTENT);
    assertEquals(bo.getData().getTest(), "Hello World");
  }

  @Test public void mapperErrorResultTest() throws Exception {
    ApiGenericResponse apiGenericResponse = apiServiceExecutor.call("error");

    BusinessObject<DataMock> bo =
        apiGenericResponseMapper.mapApiGenericResponseToBusiness(apiGenericResponse);

    assertEquals(bo.getBusinessError().getBusinessContentType(),
        BusinessContentType.BUSINESS_ERROR_CONTENT);
    assertEquals(bo.getBusinessError().getMessage(), "bad User");
  }

  @Test public void mapperBadResultTest() throws Exception {
    ApiGenericResponse apiGenericResponse = apiServiceExecutor.call("bad");

    BusinessObject<DataMock> bo =
        apiGenericResponseMapper.mapApiGenericResponseToBusiness(apiGenericResponse);

    assertEquals(bo.getBusinessError().getBusinessContentType(),
        BusinessContentType.EXCEPTION_CONTENT);
  }

  private ApiGenericResponse mockApiResponseOkClass() {
    ApiResultDataMock apiResultDataMock = new ApiResultDataMock();
    apiResultDataMock.setTest("Hello World");

    HttpResponse httpResponse = new HttpResponse(200, "OK");

    ApiResultResponseMock mockApiResponse = new ApiResultResponseMock();
    mockApiResponse.setResult(apiResultDataMock);
    mockApiResponse.setHttpResponse(httpResponse);

    return mockApiResponse;
  }

  private ApiGenericResponse mockApiResponseBusinessErrorClass() {
    ApiErrorDataMock apiErrorDataMock = new ApiErrorDataMock(1550, "bad User");
    HttpResponse httpResponse = new HttpResponse(500, "KO");

    ApiErrorResponseMock mockApiResponse = new ApiErrorResponseMock();
    mockApiResponse.setError(apiErrorDataMock);
    mockApiResponse.setHttpResponse(httpResponse);

    return mockApiResponse;
  }

  private ApiGenericResponse mockApiResponseExceptionClass() {
    Exception e = new Exception("This is a mock exception");
    ApiGenericExceptionResponse apiGenericExceptionResponse = new ApiGenericExceptionResponse(e);

    return apiGenericExceptionResponse;
  }
}
