package org.dbu.library

import org.dbu.library.model.Book
import org.dbu.library.model.Patron
import org.dbu.library.repository.InMemoryLibraryRepository
import org.dbu.library.service.BorrowResult
import org.dbu.library.service.DefaultLibraryService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LibraryServiceTest {

    private lateinit var repository: InMemoryLibraryRepository
    private lateinit var service: DefaultLibraryService

    @BeforeEach
    fun setUp() {
        repository = InMemoryLibraryRepository()
        service = DefaultLibraryService(repository)
    }

    @Test
    fun `test addBook successfully adds and prevents duplicate ISBN`() {
        val book = Book("ISBN-1", "Test Book", "Author A", 2024)
        assertTrue(service.addBook(book))
        // Attempt duplicate
        assertFalse(service.addBook(book))
    }

    @Test
    fun `test borrowBook success`() {
        val book = Book("ISBN-1", "Kotlin in Action", "Dmitry Jemerov", 2017)
        val patron = Patron("P-1", "John Doe")
        repository.addBook(book)
        repository.addPatron(patron)

        val result = service.borrowBook("P-1", "ISBN-1")
        assertEquals(BorrowResult.SUCCESS, result)

        val updatedBook = repository.findBook("ISBN-1")
        assertNotNull(updatedBook)
        assertFalse(updatedBook!!.isAvailable)

        val updatedPatron = repository.findPatron("P-1")
        assertNotNull(updatedPatron)
        assertTrue(updatedPatron!!.borrowedBooks.contains("ISBN-1"))
    }

    @Test
    fun `test borrowBook book not found`() {
        val patron = Patron("P-1", "John Doe")
        repository.addPatron(patron)

        val result = service.borrowBook("P-1", "NON_EXISTENT")
        assertEquals(BorrowResult.BOOK_NOT_FOUND, result)
    }

    @Test
    fun `test borrowBook patron not found`() {
        val book = Book("ISBN-1", "Kotlin in Action", "Dmitry Jemerov", 2017)
        repository.addBook(book)

        val result = service.borrowBook("NON_EXISTENT", "ISBN-1")
        assertEquals(BorrowResult.PATRON_NOT_FOUND, result)
    }

    @Test
    fun `test borrowBook not available`() {
        val book = Book("ISBN-1", "Kotlin in Action", "Dmitry Jemerov", 2017, isAvailable = false)
        val patron = Patron("P-1", "John Doe")
        repository.addBook(book)
        repository.addPatron(patron)

        val result = service.borrowBook("P-1", "ISBN-1")
        assertEquals(BorrowResult.NOT_AVAILABLE, result)
    }

    @Test
    fun `test borrowBook limit reached when borrowing more than 5 books`() {
        val patron = Patron("P-1", "Avid Reader", listOf("B1", "B2", "B3", "B4", "B5"))
        val book = Book("B6", "Another Book", "Author", 2020)
        repository.addPatron(patron)
        repository.addBook(book)

        val result = service.borrowBook("P-1", "B6")
        assertEquals(BorrowResult.LIMIT_REACHED, result)
    }

    @Test
    fun `test returnBook success`() {
        val book = Book("ISBN-1", "Book 1", "Author", 2021, isAvailable = false)
        val patron = Patron("P-1", "John Doe", listOf("ISBN-1"))
        repository.addBook(book)
        repository.addPatron(patron)

        val success = service.returnBook("P-1", "ISBN-1")
        assertTrue(success)

        val updatedBook = repository.findBook("ISBN-1")
        assertTrue(updatedBook!!.isAvailable)

        val updatedPatron = repository.findPatron("P-1")
        assertFalse(updatedPatron!!.borrowedBooks.contains("ISBN-1"))
    }

    @Test
    fun `test returnBook fail when patron did not borrow the book`() {
        val book = Book("ISBN-1", "Book 1", "Author", 2021, isAvailable = true)
        val patron = Patron("P-1", "John Doe", emptyList())
        repository.addBook(book)
        repository.addPatron(patron)

        val success = service.returnBook("P-1", "ISBN-1")
        assertFalse(success)
    }

    @Test
    fun `test search books by title and author case-insensitive`() {
        repository.addBook(Book("1", "Clean Architecture", "Robert Martin", 2017))
        repository.addBook(Book("2", "Design Patterns", "Erich Gamma", 1994))
        repository.addBook(Book("3", "Refactoring", "Martin Fowler", 1999))

        val searchByTitle = service.search("arch")
        assertEquals(1, searchByTitle.size)
        assertEquals("Clean Architecture", searchByTitle[0].title)

        val searchByAuthor = service.search("martin")
        assertEquals(2, searchByAuthor.size)

        val searchNone = service.search("nonexistent")
        assertTrue(searchNone.isEmpty())
    }
}
