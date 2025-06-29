package com.bibliotheque.dao;

import com.bibliotheque.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {
    private final DatabaseManager dbManager;

    public UserDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public Optional<User> findByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'utilisateur", e);
        }
        
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'utilisateur par email", e);
        }
        
        return Optional.empty();
    }

    public boolean authenticate(String username, String password) {
        Optional<User> userOpt = findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Pour la démo, on accepte "admin123" pour l'utilisateur admin
            if ("admin".equals(username) && "admin123".equals(password)) {
                return true;
            }
            // Vérification normale avec BCrypt
            try {
                return BCrypt.checkpw(password, user.getPasswordHash());
            } catch (Exception e) {
                // Si le hash n'est pas valide, essayer une comparaison directe (pour les données de test)
                return password.equals(user.getPasswordHash());
            }
        }
        
        return false;
    }

    public User save(User user) {
        if (user.getId() == 0) {
            return insert(user);
        } else {
            return update(user);
        }
    }

    private User insert(User user) {
        // Vérifier si l'utilisateur ou l'email existe déjà
        if (findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Ce nom d'utilisateur existe déjà");
        }
        
        if (findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Cette adresse email est déjà utilisée");
        }

        String insertQuery = """
            INSERT INTO users (username, email, password_hash, role, created_at)
            VALUES (?, ?, ?, ?, datetime('now'))
            """;
        
        String getIdQuery = "SELECT last_insert_rowid()";
        
        try (Connection conn = dbManager.getConnection()) {
            // Désactiver l'auto-commit pour la transaction
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getEmail());
                // Hasher le mot de passe avec BCrypt
                String hashedPassword = BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt());
                pstmt.setString(3, hashedPassword);
                pstmt.setString(4, user.getRole());
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Échec de la création de l'utilisateur");
                }
                
                // Récupérer l'ID généré
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(getIdQuery)) {
                    
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
                
                // Valider la transaction
                conn.commit();
                return user;
                
            } catch (SQLException e) {
                // Annuler la transaction en cas d'erreur
                conn.rollback();
                throw e;
            } finally {
                // Rétablir l'auto-commit
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            String errorMessage = e.getMessage().toLowerCase();
            if (errorMessage.contains("unique") || errorMessage.contains("constraint")) {
                if (errorMessage.contains("username")) {
                    throw new RuntimeException("Ce nom d'utilisateur existe déjà");
                } else if (errorMessage.contains("email")) {
                    throw new RuntimeException("Cette adresse email est déjà utilisée");
                } else {
                    throw new RuntimeException("Un compte avec ces informations existe déjà");
                }
            }
            throw new RuntimeException("Erreur lors de la création de l'utilisateur: " + e.getMessage(), e);
        }
    }

    private User update(User user) {
        String query = """
            UPDATE users SET username = ?, email = ?, password_hash = ?, role = ?
            WHERE id = ?
            """;
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getRole());
            pstmt.setInt(5, user.getId());
            
            pstmt.executeUpdate();
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage(), e);
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users ORDER BY username";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des utilisateurs", e);
        }
        
        return users;
    }

    public void deleteById(int id) {
        String query = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur", e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            // SQLite stocke les dates comme des chaînes, on peut les parser si nécessaire
            // Pour l'instant, on laisse null si on ne peut pas parser
            try {
                user.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(" ", "T")));
            } catch (Exception e) {
                // Ignorer les erreurs de parsing de date
                user.setCreatedAt(LocalDateTime.now());
            }
        }
        
        return user;
    }
}