package com.fisglobal.subscriptionapi.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;

@Document(collection = "subscribers")
public class Subscribers {
	
	@Id
	private String id;
	private String subscriberId;
	private String dateSubscribed;
	private String dateReturned="";
	private String bookId;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String notifyFlag;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	public String getDateSubscribed() {
		return dateSubscribed;
	}
	public void setDateSubscribed(String dateSubscribed) {
		this.dateSubscribed = dateSubscribed;
	}
	public String getDateReturned() {
		return dateReturned;
	}
	public void setDateReturned(String dateReturned) {
		this.dateReturned = dateReturned;
	}
	public String getBookId() {
		return bookId;
	}
	public void setBookId(String bookId) {
		this.bookId = bookId;
	}
	public String getNotifyFlag() {
		return notifyFlag;
	}
	public void setNotifyFlag(String notifyFlag) {
		this.notifyFlag = notifyFlag;
	}

}
