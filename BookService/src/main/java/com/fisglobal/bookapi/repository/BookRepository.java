package com.fisglobal.bookapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fisglobal.bookapi.entity.Books;

@Repository
public interface BookRepository extends MongoRepository<Books, String>{
	
	Books findByBookId(String bookId);

}
