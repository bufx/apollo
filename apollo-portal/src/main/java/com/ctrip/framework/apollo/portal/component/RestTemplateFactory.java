package com.ctrip.framework.apollo.portal.component;

import com.ctrip.framework.apollo.portal.component.config.PortalConfig;
import com.google.common.io.BaseEncoding;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.http.Header;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateFactory implements FactoryBean<RestTemplate>, InitializingBean {

  @Value("${eureka.auth.enabled}")
  private boolean eurekaEnabled;
  @Value("${eureka.user.name}")
  private String eurekaUserName;

  @Value("${eureka.user.password}")
  private String eurekaPassword;

  @Autowired
  private HttpMessageConverters httpMessageConverters;
  @Autowired
  private PortalConfig portalConfig;

  private RestTemplate restTemplate;

  public RestTemplate getObject() {
    return restTemplate;
  }

  public Class<RestTemplate> getObjectType() {
    return RestTemplate.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void afterPropertiesSet() throws UnsupportedEncodingException {

    CloseableHttpClient httpClient = HttpClientBuilder.create().build();;

    if (eurekaEnabled) {
      Collection<Header> defaultHeaders = new ArrayList<>();
      Header header = new BasicHeader("Authorization",
          "Basic " + BaseEncoding.base64().encode(String.join(":",eurekaUserName,eurekaPassword).getBytes("UTF-8")));
      defaultHeaders.add(header);

      BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(AuthScope.ANY,
          new UsernamePasswordCredentials(eurekaUserName, eurekaPassword));
      httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider)
              .setDefaultHeaders(defaultHeaders).build();
    }

    restTemplate = new RestTemplate(httpMessageConverters.getConverters());
    HttpComponentsClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory(httpClient);
    requestFactory.setConnectTimeout(portalConfig.connectTimeout());
    requestFactory.setReadTimeout(portalConfig.readTimeout());

    restTemplate.setRequestFactory(requestFactory);
  }


}
