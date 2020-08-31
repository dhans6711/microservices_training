package com.fisglobal.bookapi.jwt;

public class AuthenticationRequest {
	
	private String userNm;
	private String passwd;
	
	public AuthenticationRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public AuthenticationRequest(String userNm, String passwd) {
		super();
		this.userNm = userNm;
		this.passwd = passwd;
	}
	public String getUserNm() {
		return userNm;
	}
	
	public void setUserNm(String userNm) {
		this.userNm = userNm;
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

}
