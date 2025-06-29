package com.bibliotheque.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String DATABASE_URL = "jdbc:sqlite:library.db";
    
    private DatabaseManager() {
        try {
            // Charger le driver SQLite
            Class.forName("org.sqlite.JDBC");
            System.out.println("Driver SQLite chargé avec succès");
            
            // Créer la base de données si elle n'existe pas
            createDatabaseIfNotExists();
            
            // Vérifier et réparer la structure si nécessaire
            checkAndRepairStructure();
            
        } catch (ClassNotFoundException e) {
            System.err.println("Driver SQLite non trouvé: " + e.getMessage());
            throw new RuntimeException("Driver SQLite non disponible", e);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
            throw new RuntimeException("Erreur d'initialisation de la base de données", e);
        }
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            return conn;
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données:");
            System.err.println("  URL: " + DATABASE_URL);
            System.err.println("  Message: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Ferme une connexion de manière sécurisée
     * @param connection La connexion à fermer
     */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Connexion fermée avec succès");
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }
    
    /**
     * Ferme une connexion de manière statique
     * @param connection La connexion à fermer
     */
    public static void safeClose(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }
    
    /**
     * Ferme un Statement de manière sécurisée
     * @param statement Le statement à fermer
     */
    public static void safeClose(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture du statement: " + e.getMessage());
            }
        }
    }
    
    /**
     * Ferme un ResultSet de manière sécurisée
     * @param resultSet Le resultSet à fermer
     */
    public static void safeClose(java.sql.ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture du resultSet: " + e.getMessage());
            }
        }
    }
    
    /**
     * Teste la connexion à la base de données
     * @return true si la connexion fonctionne, false sinon
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Test de connexion échoué: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtient des informations sur la base de données
     * @return String avec les informations de la base
     */
    public String getDatabaseInfo() {
        try (Connection conn = getConnection()) {
            return "Base de données: " + conn.getMetaData().getDatabaseProductName() + 
                   " " + conn.getMetaData().getDatabaseProductVersion() +
                   "\nURL: " + DATABASE_URL;
        } catch (SQLException e) {
            return "Erreur lors de la récupération des informations: " + e.getMessage();
        }
    }
    
    private void createDatabaseIfNotExists() {
        try (Connection conn = getConnection()) {
            System.out.println("Base de données accessible");
        } catch (SQLException e) {
            System.err.println("Impossible d'accéder à la base de données: " + e.getMessage());
            throw new RuntimeException("Erreur d'accès à la base de données", e);
        }
    }
    
    private void checkAndRepairStructure() {
        try (Connection conn = getConnection()) {
            Statement stmt = conn.createStatement();
            
            // Vérifier si la colonne location existe
            try {
                stmt.executeQuery("SELECT location FROM books LIMIT 1").close();
                System.out.println("Structure de la base de données OK");
            } catch (SQLException e) {
                if (e.getMessage().contains("no such column: location") || 
                    e.getMessage().contains("no such table: books")) {
                    System.out.println("Réparation de la structure de la base de données...");
                    repairDatabaseStructure(stmt);
                }
            }
            
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de la structure: " + e.getMessage());
        }
    }
    
    private void repairDatabaseStructure(Statement stmt) throws SQLException {
        try {
            // Créer la table books si elle n'existe pas
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS books (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    author TEXT NOT NULL,
                    isbn TEXT UNIQUE,
                    publisher TEXT,
                    publication_year INTEGER,
                    category TEXT,
                    total_copies INTEGER DEFAULT 1,
                    available_copies INTEGER DEFAULT 1,
                    location TEXT,
                    added_date TEXT DEFAULT (date('now'))
                )
                """);
            
            // Vérifier si la colonne location existe, sinon l'ajouter
            try {
                stmt.executeQuery("SELECT location FROM books LIMIT 1").close();
            } catch (SQLException e) {
                if (e.getMessage().contains("no such column: location")) {
                    stmt.executeUpdate("ALTER TABLE books ADD COLUMN location TEXT");
                }
            }
            
            // Mettre à jour les valeurs nulles
            stmt.executeUpdate("UPDATE books SET location = 'Non spécifié' WHERE location IS NULL OR location = ''");
            
            // Créer les autres tables si nécessaire
            createOtherTables(stmt);
            
            // Insérer des données de test si les tables sont vides
            insertTestData(stmt);
            
            System.out.println("✅ Structure réparée avec succès");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la réparation: " + e.getMessage());
            throw e;
        }
    }
    
    private void createOtherTables(Statement stmt) throws SQLException {
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
    }
    
    private void insertTestData(Statement stmt) throws SQLException {
        // Vérifier si des données existent déjà
        var rs = stmt.executeQuery("SELECT COUNT(*) FROM books");
        rs.next();
        int bookCount = rs.getInt(1);
        rs.close();
        
        if (bookCount == 0) {
            // Insérer des données de test
            stmt.executeUpdate("""
                INSERT INTO users (username, email, password_hash, role) VALUES 
                ('admin', 'admin@bibliotheque.com', 'admin123', 'admin'),
                ('bibliothecaire', 'biblio@bibliotheque.com', 'biblio123', 'librarian')
                """);
            
            stmt.executeUpdate("""
                INSERT INTO members (first_name, last_name, email, phone, address) VALUES 
                ('Jean', 'Dupont', 'jean.dupont@email.com', '0123456789', '123 Rue de la Paix'),
                ('Marie', 'Martin', 'marie.martin@email.com', '0987654321', '456 Avenue des Fleurs'),
                ('Pierre', 'Durand', 'pierre.durand@email.com', '0147258369', '789 Boulevard du Centre')
                """);
            
            stmt.executeUpdate("""
                INSERT INTO books (title, author, isbn, publisher, publication_year, category, total_copies, available_copies, location) VALUES 
                ('Le Petit Prince', 'Antoine de Saint-Exupéry', '978-2070408504', 'Gallimard', 1943, 'Fiction', 3, 3, 'Rayon A-1'),
                ('1984', 'George Orwell', '978-0451524935', 'Signet Classics', 1949, 'Fiction', 2, 2, 'Rayon A-2'),
                ('Les Misérables', 'Victor Hugo', '978-2253096337', 'Le Livre de Poche', 1862, 'Classique', 2, 1, 'Rayon B-1'),
                ('Harry Potter à l''école des sorciers', 'J.K. Rowling', '978-2070541270', 'Gallimard Jeunesse', 1997, 'Fantasy', 4, 3, 'Rayon C-1')
                """);
            
            System.out.println("✅ Données de test insérées");
        }
    }

    /**
     * Méthode pour nettoyer les ressources globales lors de la fermeture de l'application
     */
    public void shutdown() {
        try {
            // Ici on peut ajouter du nettoyage global si nécessaire
            System.out.println("DatabaseManager: Nettoyage des ressources terminé");
        } catch (Exception e) {
            System.err.println("Erreur lors du nettoyage: " + e.getMessage());
        }
    }
}
