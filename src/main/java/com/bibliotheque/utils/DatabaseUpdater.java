package com.bibliotheque.utils;

import com.bibliotheque.dao.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUpdater {
    
    public static void main(String[] args) {
        System.out.println("=== Mise à jour de la base de données ===");
        
        DatabaseUpdater updater = new DatabaseUpdater();
        updater.checkAndUpdateDatabase();
    }
    
    public void checkAndUpdateDatabase() {
        try {
            // Vérifier la structure actuelle
            checkCurrentStructure();
            
            // Mettre à jour si nécessaire
            updateDatabaseStructure();
            
            // Vérifier après mise à jour
            verifyStructure();
            
            System.out.println("✅ Base de données mise à jour avec succès!");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void checkCurrentStructure() {
        System.out.println("\n1. Vérification de la structure actuelle...");
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            
            // Vérifier les tables existantes
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            
            System.out.println("Tables existantes:");
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("  - " + tableName);
                
                if ("books".equals(tableName)) {
                    checkBooksTableStructure(conn);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification: " + e.getMessage());
        }
    }
    
    private void checkBooksTableStructure(Connection conn) throws SQLException {
        System.out.println("    Structure de la table 'books':");
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("PRAGMA table_info(books)");
        
        List<String> columns = new ArrayList<>();
        while (rs.next()) {
            String columnName = rs.getString("name");
            String columnType = rs.getString("type");
            columns.add(columnName);
            System.out.println("      - " + columnName + " (" + columnType + ")");
        }
        
        // Vérifier si la colonne location existe
        if (!columns.contains("location")) {
            System.out.println("    ⚠️  Colonne 'location' manquante!");
        } else {
            System.out.println("    ✅ Colonne 'location' présente");
        }
        
        rs.close();
        stmt.close();
    }
    
    private void updateDatabaseStructure() {
        System.out.println("\n2. Mise à jour de la structure...");
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Vérifier si la colonne location existe
                if (!columnExists(conn, "books", "location")) {
                    System.out.println("   Ajout de la colonne 'location'...");
                    
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate("ALTER TABLE books ADD COLUMN location TEXT");
                    stmt.executeUpdate("UPDATE books SET location = 'Non spécifié' WHERE location IS NULL");
                    stmt.close();
                    
                    System.out.println("   ✅ Colonne 'location' ajoutée");
                }
                
                // Créer les autres tables si elles n'existent pas
                createMissingTables(conn);
                
                conn.commit();
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")");
        
        while (rs.next()) {
            if (columnName.equals(rs.getString("name"))) {
                rs.close();
                stmt.close();
                return true;
            }
        }
        
        rs.close();
        stmt.close();
        return false;
    }
    
    private void createMissingTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        
        // Table users
        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                email TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'librarian',
                created_at TEXT DEFAULT (datetime('now')),
                CONSTRAINT chk_role CHECK (role IN ('admin', 'librarian'))
            )
            """);
        
        // Table members
        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS members (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                first_name TEXT NOT NULL,
                last_name TEXT NOT NULL,
                email TEXT UNIQUE,
                phone TEXT,
                address TEXT,
                membership_date TEXT DEFAULT (date('now')),
                status TEXT DEFAULT 'active',
                CONSTRAINT chk_status CHECK (status IN ('active', 'inactive', 'suspended'))
            )
            """);
        
        // Table loans
        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS loans (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                book_id INTEGER NOT NULL,
                member_id INTEGER NOT NULL,
                loan_date TEXT DEFAULT (date('now')),
                due_date TEXT NOT NULL,
                return_date TEXT,
                status TEXT DEFAULT 'active',
                notes TEXT,
                FOREIGN KEY (book_id) REFERENCES books(id),
                FOREIGN KEY (member_id) REFERENCES members(id),
                CONSTRAINT chk_loan_status CHECK (status IN ('active', 'returned', 'overdue'))
            )
            """);
        
        stmt.close();
        System.out.println("   ✅ Tables vérifiées/créées");
    }
    
    private void verifyStructure() {
        System.out.println("\n3. Vérification finale...");
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            
            // Test de la table books avec la colonne location
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, title, author, location FROM books LIMIT 1");
            
            if (rs.next()) {
                System.out.println("   ✅ Table 'books' avec colonne 'location' fonctionne");
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            System.err.println("   ❌ Erreur lors de la vérification finale: " + e.getMessage());
        }
    }
}
