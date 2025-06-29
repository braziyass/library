package com.bibliotheque.dao;

import com.bibliotheque.model.Loan;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoanDAO {
    private final DatabaseManager dbManager;

    public LoanDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public Loan save(Loan loan) {
        if (loan.getId() == 0) {
            return insert(loan);
        } else {
            return update(loan);
        }
    }

    private Loan insert(Loan loan) {
        String query = """
            INSERT INTO loans (book_id, member_id, loan_date, due_date, status, notes)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, loan.getBookId());
                pstmt.setInt(2, loan.getMemberId());
                pstmt.setString(3, loan.getLoanDate().toString());
                pstmt.setString(4, loan.getDueDate().toString());
                pstmt.setString(5, loan.getStatus());
                pstmt.setString(6, loan.getNotes());

                pstmt.executeUpdate();

                // Récupérer l'ID généré
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        loan.setId(rs.getInt(1));
                    }
                }

                // Décrémenter le nombre de copies disponibles
                updateBookAvailability(conn, loan.getBookId(), -1);

                conn.commit();
                return loan;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de l'emprunt: " + e.getMessage(), e);
        }
    }

    private Loan update(Loan loan) {
        String query = """
            UPDATE loans SET book_id = ?, member_id = ?, loan_date = ?, due_date = ?, 
                           return_date = ?, status = ?, notes = ?
            WHERE id = ?
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, loan.getBookId());
            pstmt.setInt(2, loan.getMemberId());
            pstmt.setString(3, loan.getLoanDate().toString());
            pstmt.setString(4, loan.getDueDate().toString());
            pstmt.setString(5, loan.getReturnDate() != null ? loan.getReturnDate().toString() : null);
            pstmt.setString(6, loan.getStatus());
            pstmt.setString(7, loan.getNotes());
            pstmt.setInt(8, loan.getId());

            pstmt.executeUpdate();
            return loan;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'emprunt: " + e.getMessage(), e);
        }
    }

    public Loan returnBook(int loanId) {
        String query = """
            UPDATE loans SET return_date = ?, status = 'returned'
            WHERE id = ? AND status = 'active'
            """;

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);

            // Récupérer l'emprunt avant mise à jour
            Optional<Loan> loanOpt = findById(loanId);
            if (loanOpt.isEmpty()) {
                throw new RuntimeException("Emprunt non trouvé");
            }

            Loan loan = loanOpt.get();

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, LocalDate.now().toString());
                pstmt.setInt(2, loanId);

                int updated = pstmt.executeUpdate();
                if (updated == 0) {
                    throw new RuntimeException("Emprunt non trouvé ou déjà retourné");
                }

                // Incrémenter le nombre de copies disponibles
                updateBookAvailability(conn, loan.getBookId(), 1);

                conn.commit();

                // Mettre à jour l'objet loan
                loan.setReturnDate(LocalDate.now());
                loan.setStatus("returned");
                return loan;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du retour du livre: " + e.getMessage(), e);
        }
    }

    private void updateBookAvailability(Connection conn, int bookId, int change) throws SQLException {
        String query = "UPDATE books SET available_copies = available_copies + ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, change);
            pstmt.setInt(2, bookId);
            pstmt.executeUpdate();
        }
    }

    public List<Loan> findAll() {
        List<Loan> loans = new ArrayList<>();
        String query = """
            SELECT l.*, b.title as book_title, b.author as book_author,
                   m.first_name || ' ' || m.last_name as member_name
            FROM loans l
            JOIN books b ON l.book_id = b.id
            JOIN members m ON l.member_id = m.id
            ORDER BY l.loan_date DESC
            """;

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                loans.add(mapResultSetToLoan(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des emprunts", e);
        }

        return loans;
    }

    public Optional<Loan> findById(int id) {
        String query = """
            SELECT l.*, b.title as book_title, b.author as book_author,
                   m.first_name || ' ' || m.last_name as member_name
            FROM loans l
            JOIN books b ON l.book_id = b.id
            JOIN members m ON l.member_id = m.id
            WHERE l.id = ?
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToLoan(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'emprunt", e);
        }

        return Optional.empty();
    }

    public List<Loan> findActiveLoans() {
        List<Loan> loans = new ArrayList<>();
        String query = """
            SELECT l.*, b.title as book_title, b.author as book_author,
                   m.first_name || ' ' || m.last_name as member_name
            FROM loans l
            JOIN books b ON l.book_id = b.id
            JOIN members m ON l.member_id = m.id
            WHERE l.status = 'active'
            ORDER BY l.due_date ASC
            """;

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                loans.add(mapResultSetToLoan(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des emprunts actifs", e);
        }

        return loans;
    }

    public List<Loan> findOverdueLoans() {
        List<Loan> loans = new ArrayList<>();
        String query = """
            SELECT l.*, b.title as book_title, b.author as book_author,
                   m.first_name || ' ' || m.last_name as member_name
            FROM loans l
            JOIN books b ON l.book_id = b.id
            JOIN members m ON l.member_id = m.id
            WHERE l.status = 'active' AND l.due_date < date('now')
            ORDER BY l.due_date ASC
            """;

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                loans.add(mapResultSetToLoan(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des emprunts en retard", e);
        }

        return loans;
    }

    private Loan mapResultSetToLoan(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setId(rs.getInt("id"));
        loan.setBookId(rs.getInt("book_id"));
        loan.setMemberId(rs.getInt("member_id"));
        loan.setStatus(rs.getString("status"));
        loan.setNotes(rs.getString("notes"));

        // Champs de jointure
        loan.setBookTitle(rs.getString("book_title"));
        loan.setBookAuthor(rs.getString("book_author"));
        loan.setMemberName(rs.getString("member_name"));

        // Dates
        String loanDateStr = rs.getString("loan_date");
        if (loanDateStr != null) {
            try {
                loan.setLoanDate(LocalDate.parse(loanDateStr));
            } catch (Exception e) {
                loan.setLoanDate(LocalDate.now());
            }
        }

        String dueDateStr = rs.getString("due_date");
        if (dueDateStr != null) {
            try {
                loan.setDueDate(LocalDate.parse(dueDateStr));
            } catch (Exception e) {
                loan.setDueDate(LocalDate.now().plusDays(14));
            }
        }

        String returnDateStr = rs.getString("return_date");
        if (returnDateStr != null) {
            try {
                loan.setReturnDate(LocalDate.parse(returnDateStr));
            } catch (Exception e) {
                // Ignorer les erreurs de parsing
            }
        }

        return loan;
    }
}