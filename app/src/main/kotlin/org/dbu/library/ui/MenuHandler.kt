package org.dbu.library.ui

import org.dbu.library.model.Book
import org.dbu.library.model.Patron
import org.dbu.library.repository.LibraryRepository
import org.dbu.library.service.BorrowResult
import org.dbu.library.service.LibraryService

fun handleMenuAction(
    choice: String,
    service: LibraryService,
    repository: LibraryRepository
): Boolean {

    return when (choice) {

        "1" -> {
            addBook(service)
            true
        }

        "2" -> {
            registerPatron(repository)
            true
        }

        "3" -> {
            borrowBook(service)
            true
        }

        "4" -> {
            returnBook(service)
            true
        }

        "5" -> {
            search(service)
            true
        }

        "6" -> {
            listAllBooks(repository)
            true
        }

        "0" -> false

        else -> {
            println("Invalid option")
            true
        }
    }
}

private fun addBook(service: LibraryService) {
    print("ISBN: ")
    val isbn = readln().trim()
    print("Title: ")
    val title = readln().trim()
    print("Author: ")
    val author = readln().trim()
    print("Year: ")
    val year = readln().trim().toIntOrNull()
    if (year == null) {
        println("Invalid year")
        return
    }
    val result = service.addBook(Book(isbn, title, author, year))
    println("Book added: $result")
}

private fun registerPatron(repository: LibraryRepository) {
    print("Patron ID: ")
    val id = readln().trim()
    print("Name: ")
    val name = readln().trim()
    val result = repository.addPatron(Patron(id, name))
    println("Patron added: $result")
}

private fun borrowBook(service: LibraryService) {
    print("Patron ID: ")
    val patronId = readln().trim()
    print("ISBN: ")
    val isbn = readln().trim()

    val result = service.borrowBook(patronId, isbn)
    when (result) {
        BorrowResult.SUCCESS -> println("Borrowed")
        BorrowResult.BOOK_NOT_FOUND -> println("Book not found")
        BorrowResult.PATRON_NOT_FOUND -> println("Patron not found")
        BorrowResult.NOT_AVAILABLE -> println("Book not available")
        BorrowResult.LIMIT_REACHED -> println("Limit reached")
    }
}

private fun returnBook(service: LibraryService) {
    print("Patron ID: ")
    val patronId = readln().trim()
    print("ISBN: ")
    val isbn = readln().trim()

    val success = service.returnBook(patronId, isbn)
    if (success) {
        println("Returned")
    } else {
        println("Return failed")
    }
}

private fun search(service: LibraryService) {
    print("Query: ")
    val query = readln().trim()
    val books = service.search(query)
    if (books.isEmpty()) {
        println("No books found")
        return
    }
    books.forEachIndexed { index, book ->
        println("${index + 1}. ${book.title} - ${book.author}")
    }
}

private fun listAllBooks(repository: LibraryRepository) {
    val books = repository.getAllBooks()
    if (books.isEmpty()) {
        println("No books in library")
        return
    }
    books.forEach { book ->
        val status = if (book.isAvailable) "Available" else "Borrowed"
        println("${book.isbn} | ${book.title} | $status")
    }
}