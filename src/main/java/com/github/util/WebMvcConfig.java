package com.github.util;

import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Resource private FastJsonConfig fastJsonConfig;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**");
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(false);
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

		FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
		fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
		fastJsonHttpMessageConverter.setSupportedMediaTypes(new ArrayList<MediaType>() {
			private static final long serialVersionUID = 6666659690562975474L;
			{
				this.add(MediaType.APPLICATION_JSON);
				this.add(MediaType.APPLICATION_JSON_UTF8);
			}});
		converters.set(7, fastJsonHttpMessageConverter);
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new Converter<String, Date>() {
			@Override
			public Date convert(String source) {
				if (StringUtils.isNotEmpty(source)) {
					try {
						return DateUtils.parseDate(source, new String[]{"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss"});
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		});
	}


	@Bean
	public WebMvcRegistrations webMvcRegistrations() {
		return new WebMvcRegistrations() {
			@Override
			public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
				return new EnhanceRequestMappingHandlerMapping();
			}
		};
	}

	private static final class EnhanceRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
		@Override
		protected boolean isHandler(Class<?> beanType) {
			return super.isHandler(beanType) && beanType.getPackage().getName().contains("controller");
		}
	}
}

