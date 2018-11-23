package com.github.event;

import com.github.model.Feedback;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TaskProcessEventListener implements SmartApplicationListener {

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return TaskProcessEvent.class == eventType;
	}

	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return true;
	}

	@Async
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		TaskProcessEvent taskProcessEvent = (TaskProcessEvent) event;
		Feedback feedback = taskProcessEvent.getFeedback();
		System.err.println(feedback);
	}

	@Override
	public int getOrder() {
		return 0;
	}
}
