package com.github.util;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class HttpClientConfig {


	// Determines the timeout in milliseconds until a connection is established.
	private static final int CONNECT_TIMEOUT = 30 * 1000;

	// The timeout when requesting a connection from the connection manager.
	private static final int REQUEST_TIMEOUT = 30 * 1000;

	// The timeout for waiting for data
	private static final int SOCKET_TIMEOUT = 60 * 1000;

	private static final int MAX_TOTAL_CONNECTIONS = 128;
	private static final int DEFAULT_KEEP_ALIVE_TIME_MILLIS = 20 * 1000;
	private static final int CLOSE_IDLE_CONNECTION_WAIT_TIME = 30 * 60 * 1000;

	private static PoolingHttpClientConnectionManager connectionManager = null;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	@Bean
	public HttpClient httpClient() {

		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(SOCKET_TIMEOUT) // 服务器返回数据(response)的时间，超过该时间抛出read timeout
				.setConnectTimeout(CONNECT_TIMEOUT)// 连接上服务器(握手成功)的时间，超出该时间抛出connect timeout
				.setConnectionRequestTimeout(REQUEST_TIMEOUT) // 从连接池中获取连接的超时时间，超过该时间未拿到可用连接，会抛出org.apache.http.conn.ConnectionPoolTimeoutException: Timeout waiting for connection from pool
//				.setProxy()
				.build();

//		HttpHost proxy = new HttpHost("127.0.0.1", 8888, "HTTP");
//		HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy); // http代理
//		HttpRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault()); // 系统代理

		HttpClientBuilder httpClientBuilder = HttpClients.custom()
//				.setRoutePlanner(routePlanner)
				.setDefaultRequestConfig(requestConfig)
				.setConnectionManager(poolingConnectionManager())
				.setKeepAliveStrategy(connectionKeepAliveStrategy());


		List<Header> defaultHeaders = Arrays.asList(
				new BasicHeader("X-Default-Header", "default header httpclient"),
				new BasicHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36")
		);

		httpClientBuilder.setDefaultHeaders(defaultHeaders);

		return httpClientBuilder.build();
	}


	@Bean
	public PoolingHttpClientConnectionManager poolingConnectionManager() {

		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.getSocketFactory())
				.register("https", SSLConnectionSocketFactory.getSocketFactory())
				.build();

		PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);


		poolingConnectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS); // 设置整个连接池最大连接数 根据自己的场景决定
		poolingConnectionManager.setDefaultMaxPerRoute(2 * MAX_TOTAL_CONNECTIONS); // 路由是对maxTotal的细分

		connectionManager = poolingConnectionManager;
		return poolingConnectionManager;
	}

	@Bean
	public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
		// ConnectionKeepAliveStrategy helps in setting time which decide how long a connection can remain idle before being reused.
		return new ConnectionKeepAliveStrategy() {
			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				HeaderElementIterator it = new BasicHeaderElementIterator (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
				while (it.hasNext()) {
					HeaderElement he = it.nextElement();
					String param = he.getName();
					String value = he.getValue();

					if (value != null && param.equalsIgnoreCase("timeout")) {
						return Long.parseLong(value) * 1000;
					}
				}
				return DEFAULT_KEEP_ALIVE_TIME_MILLIS;
			}
		};
	}


	@Bean
	public Object idleConnectionMonitor() {
		return new Object() {
			@Scheduled(fixedDelay = 30 * 60 * 1000)
			public void run() {
				if (connectionManager != null) {
					logger.trace("run IdleConnectionMonitor - Closing expired and idle connections...");
					connectionManager.closeExpiredConnections();
					connectionManager.closeIdleConnections(CLOSE_IDLE_CONNECTION_WAIT_TIME, TimeUnit.MILLISECONDS);
				} else {
					logger.trace("run IdleConnectionMonitor - Http Client Connection manager is not initialised");
				}
			}
		};
	}

}