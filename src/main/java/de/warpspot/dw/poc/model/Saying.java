package de.warpspot.dw.poc.model;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Saying {
	private long id;
	
	@Length(max = 3)
	private String content;
	
	public Saying() {
	}

	public Saying(long pId, String pContent) {
		this.id = pId;
		this.content = pContent;
	}
	
	@JsonProperty
	public long getId() {
		return id;
	}
	
	@JsonProperty
	public String getContent() {
		return content;
	}
}
