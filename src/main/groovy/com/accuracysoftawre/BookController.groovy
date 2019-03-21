package com.accuracysoftawre

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpResponse

import grails.gorm.transactions.Transactional
import grails.gorm.annotation.Entity

@Entity
class Book {
  static graphql = true
  String name
  String author
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

