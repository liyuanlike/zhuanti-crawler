package com.github.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;

@Service
public class TaskExecutor implements InitializingBean {

	@Resource private Executor executor;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private static CompletionService<?> completionService = null;

	@Override
	public void afterPropertiesSet() {
		completionService = new ExecutorCompletionService<>(executor);
	}

	public String getUrlContent(String url) {
		return null;
	}
}

