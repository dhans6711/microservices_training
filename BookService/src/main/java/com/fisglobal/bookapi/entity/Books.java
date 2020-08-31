package com.fisglobal.bookapi.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "books")
public class Books {
	
	@Id
	private String id;
	private String bookId;
	private String bookName;
	private String author;
	private int availableCopies;
	private int totalCopies;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBookId() {
		return bookId;
	}
	public void setBookId(String bookId) {
		this.bookId = bookId;
	}
	public String getBookName() {
		return bookName;
	}
	public void setBookName(String bookName) {
		this.bookName = bookName;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public int getAvailableCopies() {
		return availableCopies;
	}
	public void setAvailableCopies(int availableCopies) {
		this.availableCopies = availableCopies;
	}
	public int getTotalCopies() {
		return totalCopies;
	}
	public void setTotalCopies(int totalCopies) {
		this.totalCopies = totalCopies;
	}
	
}
