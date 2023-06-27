package com.capmation.challenge2;

import java.util.Date;

import org.springframework.data.annotation.Id;

/**
 * Note Entity
 * @author diegoflores
 *
 */
public class Note {
	@Id
	private Long id;
	private String title;
	private String body;
	private Date createdOn;
	private Date modifiedOn;
	private String owner;
	
	public Note(Long id, String title, String body, Date createdOn, Date modifiedOn, String owner) {
		super();
		this.id = id;
		this.title = title;
		this.body = body;
		this.createdOn = createdOn;
		this.modifiedOn = modifiedOn;
		this.owner = owner;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Date getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(Date modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public Long getId() {
		return id;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public String getOwner() {
		return owner;
	}
	
}
