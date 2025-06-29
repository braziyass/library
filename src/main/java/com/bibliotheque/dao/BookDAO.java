package com.bibliotheque.dao;

import com.bibliotheque.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDAO {
    private final DatabaseManager dbManager;

    public BookDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        } else {
            return update(book);
        }
    }

    private Book insert(Book book) {
        String query = """
            INSERT INTO books (title, author, isbn, publisher, publication_year, category, 
                             total_copies, available_copies, location)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getIsbn());
            pstmt.setString(4, book.getPublisher());
            pstmt.setInt(5, book.getPublicationYear());
            pstmt.setString(6, book.getCategory());
            pstmt.setInt(7, book.getTotalCopies());
            pstmt.setInt(8, book.getAvailableCopies());
            pstmt.setString(9, book.getLocation());

            pstmt.executeUpdate();

            // Récupérer l'ID généré
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    book.setId(rs.getInt(1));
                }
            }

            return book;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création du livre: " + e.getMessage(), e);
        }
    }

    private Book update(Book book) {
        String query = """
            UPDATE books SET title = ?, author = ?, isbn = ?, publisher = ?, 
                           publication_year = ?, category = ?, total_copies = ?, 
                           available_copies = ?, location = ?
            WHERE id = ?
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getIsbn());
            pstmt.setString(4, book.getPublisher());
            pstmt.setInt(5, book.getPublicationYear());
            pstmt.setString(6, book.getCategory());
            pstmt.setInt(7, book.getTotalCopies());
            pstmt.setInt(8, book.getAvailableCopies());
            pstmt.setString(9, book.getLocation());
            pstmt.setInt(10, book.getId());

            pstmt.executeUpdate();
            return book;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du livre: " + e.getMessage(), e);
        }
    }

    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books ORDER BY title";

        try (Connection conn = dbManager.getConnection()) {
            System.out.println("Connexion à la base de données réussie"); // Debug
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
                
                System.out.println("Nombre de livres trouvés: " + books.size()); // Debug
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage()); // Debug
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des livres", e);
        }

        return books;
    }


    public List<Book> findAvailableBooks() {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books WHERE available_copies > 0 ORDER BY title";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des livres disponibles", e);
        }

        return books;
    }

    public Optional<Book> findById(int id) {
        String query = "SELECT * FROM books WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du livre", e);
        }

        return Optional.empty();
    }

    public List<Book> search(String searchTerm) {
        List<Book> books = new ArrayList<>();
        String query = """
            SELECT * FROM books 
            WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? OR category LIKE ?
            ORDER BY title
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des livres", e);
        }

        return books;
    }

    public void deleteById(int id) {
        String query = "DELETE FROM books WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du livre", e);
        }
    }

    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setPublisher(rs.getString("publisher"));
        book.setPublicationYear(rs.getInt("publication_year"));
        book.setCategory(rs.getString("category"));
        book.setTotalCopies(rs.getInt("total_copies"));
        book.setAvailableCopies(rs.getInt("available_copies"));
        book.setLocation(rs.getString("location"));

        String addedDateStr = rs.getString("added_date");
        if (addedDateStr != null) {
            try {
                book.setAddedDate(java.time.LocalDate.parse(addedDateStr));
            } catch (Exception e) {
                book.setAddedDate(java.time.LocalDate.now());
            }
        }

        return book;
    }
}