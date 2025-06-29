-- Création des tables pour la gestion de bibliothèque

-- Table des utilisateurs
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'librarian',
    created_at TEXT DEFAULT (datetime('now')),
    CONSTRAINT chk_role CHECK (role IN ('admin', 'librarian'))
);

-- Table des membres de la bibliothèque
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
);

-- Table des livres
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
);

-- Table des emprunts
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
);

-- Insertion des données de test
INSERT OR IGNORE INTO users (username, email, password_hash, role) VALUES 
('admin', 'admin@bibliotheque.com', 'admin123', 'admin'),
('bibliothecaire', 'biblio@bibliotheque.com', 'biblio123', 'librarian');

INSERT OR IGNORE INTO members (first_name, last_name, email, phone, address) VALUES 
('Jean', 'Dupont', 'jean.dupont@email.com', '0123456789', '123 Rue de la Paix'),
('Marie', 'Martin', 'marie.martin@email.com', '0987654321', '456 Avenue des Fleurs'),
('Pierre', 'Durand', 'pierre.durand@email.com', '0147258369', '789 Boulevard du Centre');

INSERT OR IGNORE INTO books (title, author, isbn, publisher, publication_year, category, total_copies, available_copies) VALUES 
('Le Petit Prince', 'Antoine de Saint-Exupéry', '978-2070408504', 'Gallimard', 1943, 'Fiction', 3, 3),
('1984', 'George Orwell', '978-0451524935', 'Signet Classics', 1949, 'Fiction', 2, 2),
('Les Misérables', 'Victor Hugo', '978-2253096337', 'Le Livre de Poche', 1862, 'Classique', 2, 1),
('Harry Potter à l''école des sorciers', 'J.K. Rowling', '978-2070541270', 'Gallimard Jeunesse', 1997, 'Fantasy', 4, 3);

-- Quelques emprunts de test
INSERT OR IGNORE INTO loans (book_id, member_id, due_date, status) VALUES 
(3, 1, date('now', '+14 days'), 'active'),
(4, 2, date('now', '+7 days'), 'active');