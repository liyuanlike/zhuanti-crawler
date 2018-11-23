package com.github.event;

import com.github.model.Feedback;
import org.springframework.context.ApplicationEvent;

public class TaskProcessEvent extends ApplicationEvent {

	private Feedback feedback;

	public TaskProcessEvent(Object source, Feedback feedback) {
		super(source);
		this.feedback = feedback;
	}

	public Feedback getFeedback() {
		return feedback;
	}

	public void setFeedback(Feedback feedback) {
		this.feedback = feedback;
	}
}
