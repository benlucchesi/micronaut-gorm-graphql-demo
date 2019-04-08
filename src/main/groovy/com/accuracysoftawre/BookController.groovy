package com.accuracysoftawre

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpResponse

import grails.gorm.transactions.Transactional
import grails.gorm.annotation.Entity
import org.grails.datastore.gorm.GormEntity

import graphql.schema.DataFetchingEnvironment

import org.grails.gorm.graphql.fetcher.impl.CreateEntityDataFetcher
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping
import org.grails.datastore.mapping.model.PersistentEntity


@Entity
class Book implements GormEntity<Book> { 
  static graphql = true
  static hasMany = [authors: Author, keyWords: String]
  static hasOne = [isbn:ISBN]
  static belongsTo = [authors: Author]
  
  static mapping = {
    authors lazy: false
    keyWords lazy: false
    isbn lazy: false
  }

  static constraints = {
    title unique: true
  }

  Library library

  String title
  List<String> keyWords = []
}

class AuthorCreateEntityDataFetcher extends CreateEntityDataFetcher<Author>{

  AuthorCreateEntityDataFetcher(PersistentEntity entity) {
    super(entity, null)
  }

  @Override
  Author get(DataFetchingEnvironment environment) {
    withTransaction(false) {
      GormEntity instance = newInstance
      dataBinder.bind(instance, getArgument(environment))
      try{
        instance.save(true)
        return instance
      }
      catch( excp ){
        println instance.errors.allErrors
         throw new RuntimeException("Failed to save. ${excp}")
      }
    }
  }

}


@Entity
class Author implements GormEntity<Author> {

  // static graphql = true

  static graphql = GraphQLMapping.lazy{
    mutation('authorCreate', Author){
      argument('author', Author)
      dataFetcher(new AuthorCreateEntityDataFetcher(Author.gormPersistentEntity))
    }
  }

  static constraints = {
    fullName unique: true, email: true
  }
  static mapping = {
    books lazy: false
  }
  static embedded = ["address"]
  static hasMany = [books: Book]
  String fullName
  Address address
}

@Entity
class ISBN {
  static graphql = true
  String encodedNumber
  String region
  Book book   
}

@Entity
class Library {
  static graphql = true
  String name
  // static hasMany = [books: Book]
  static hasMany = [addresses: Address, keyWords: String]
  static embedded = ["keyWords"]
  // List<Address> addresses
}

@Entity
class Address {
  String street
}

@Transactional
@Controller("/book")
class BookController {

    @Get("/")
    HttpStatus index() {
        return HttpStatus.OK
    }

    @Post("/save")
    def save(@Body Book book) {
      println "received a book in the body..."
      println book
      // return HttpStatus.OK
      return book.save()
    }

    @Get("/list")
    def listBooks() {
      def books = Book.list()
      println books
      return books
    }

}

