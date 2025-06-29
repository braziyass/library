package com.bibliotheque.dao;

import com.bibliotheque.model.Member;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberDAO {
    private final DatabaseManager dbManager;

    public MemberDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public Member save(Member member) {
        if (member.getId() == 0) {
            return insert(member);
        } else {
            return update(member);
        }
    }

    private Member insert(Member member) {
        String query = """
            INSERT INTO members (first_name, last_name, email, phone, address, membership_date, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, member.getFirstName());
            pstmt.setString(2, member.getLastName());
            pstmt.setString(3, member.getEmail());
            pstmt.setString(4, member.getPhone());
            pstmt.setString(5, member.getAddress());
            pstmt.setString(6, member.getMembershipDate().toString());
            pstmt.setString(7, member.getStatus());

            pstmt.executeUpdate();

            // Récupérer l'ID généré
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    member.setId(rs.getInt(1));
                }
            }

            return member;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création du membre: " + e.getMessage(), e);
        }
    }

    private Member update(Member member) {
        String query = """
            UPDATE members SET first_name = ?, last_name = ?, email = ?, phone = ?, 
                             address = ?, status = ?
            WHERE id = ?
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, member.getFirstName());
            pstmt.setString(2, member.getLastName());
            pstmt.setString(3, member.getEmail());
            pstmt.setString(4, member.getPhone());
            pstmt.setString(5, member.getAddress());
            pstmt.setString(6, member.getStatus());
            pstmt.setInt(7, member.getId());

            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du membre: " + e.getMessage(), e);
        }
    }

    public List<Member> findAll() {
        List<Member> members = new ArrayList<>();
        String query = "SELECT * FROM members ORDER BY last_name, first_name";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des membres", e);
        }

        return members;
    }

    public Optional<Member> findById(int id) {
        String query = "SELECT * FROM members WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMember(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du membre", e);
        }

        return Optional.empty();
    }

    public List<Member> search(String searchTerm) {
        List<Member> members = new ArrayList<>();
        String query = """
            SELECT * FROM members 
            WHERE first_name LIKE ? OR last_name LIKE ? OR email LIKE ?
            ORDER BY last_name, first_name
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    members.add(mapResultSetToMember(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des membres", e);
        }

        return members;
    }

    public void deleteById(int id) {
        String query = "DELETE FROM members WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du membre", e);
        }
    }

    private Member mapResultSetToMember(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setId(rs.getInt("id"));
        member.setFirstName(rs.getString("first_name"));
        member.setLastName(rs.getString("last_name"));
        member.setEmail(rs.getString("email"));
        member.setPhone(rs.getString("phone"));
        member.setAddress(rs.getString("address"));
        member.setStatus(rs.getString("status"));

        String membershipDateStr = rs.getString("membership_date");
        if (membershipDateStr != null) {
            try {
                member.setMembershipDate(LocalDate.parse(membershipDateStr));
            } catch (Exception e) {
                member.setMembershipDate(LocalDate.now());
            }
        }

        return member;
    }
}