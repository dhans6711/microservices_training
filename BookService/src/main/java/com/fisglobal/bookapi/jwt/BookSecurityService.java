package com.fisglobal.bookapi.jwt;

import java.util.ArrayList;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class BookSecurityService implements UserDetailsService{

	@Override
	public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException{
		return new User("books","books",new ArrayList<>());
	}
}
