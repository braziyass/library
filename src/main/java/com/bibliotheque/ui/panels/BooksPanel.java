package com.bibliotheque.ui.panels;

import com.bibliotheque.dao.BookDAO;
import com.bibliotheque.model.Book;
import com.bibliotheque.ui.dialogs.BookDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BooksPanel extends JPanel {
    private BookDAO bookDAO;
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private TableRowSorter<DefaultTableModel> sorter;

    public BooksPanel() {
        this.bookDAO = new BookDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadBooks();
    }

    private void initializeComponents() {
        // Champ de recherche
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(250, 35));

        // Boutons
        addButton = createButton("Ajouter", new Color(34, 139, 34), "➕");
        editButton = createButton("Modifier", new Color(70, 130, 180), "✏️");
        deleteButton = createButton("Supprimer", new Color(220, 20, 60), "🗑️");
        refreshButton = createButton("Actualiser", new Color(128, 128, 128), "🔄");

        // Table
        String[] columnNames = {"ID", "Titre", "Auteur", "ISBN", "Éditeur", "Année", "Catégorie", "Total", "Disponibles", "Emplacement"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        booksTable = new JTable(tableModel);
        booksTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        booksTable.setRowHeight(25);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        booksTable.setGridColor(new Color(230, 230, 230));
        booksTable.setShowGrid(true);

        // Configuration des colonnes
        booksTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        booksTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Titre
        booksTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Auteur
        booksTable.getColumnModel().getColumn(3).setPreferredWidth(120); // ISBN
        booksTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Éditeur
        booksTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Année
        booksTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Catégorie
        booksTable.getColumnModel().getColumn(7).setPreferredWidth(60);  // Total
        booksTable.getColumnModel().getColumn(8).setPreferredWidth(80);  // Disponibles
        booksTable.getColumnModel().getColumn(9).setPreferredWidth(120); // Emplacement

        // Tri
        sorter = new TableRowSorter<>(tableModel);
        booksTable.setRowSorter(sorter);

        // Masquer la colonne ID
        booksTable.getColumnModel().getColumn(0).setMinWidth(0);
        booksTable.getColumnModel().getColumn(0).setMaxWidth(0);
        booksTable.getColumnModel().getColumn(0).setWidth(0);
    }

    private JButton createButton(String text, Color color, String icon) {
        JButton button = new JButton(icon + " " + text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(120, 35));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panneau du haut
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Titre
        JLabel titleLabel = new JLabel("Gestion des Livres");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Panneau de recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel searchLabel = new JLabel("Rechercher:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        topPanel.add(searchPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table avec scroll
        JScrollPane scrollPane = new JScrollPane(booksTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Liste des livres"));
        add(scrollPane, BorderLayout.CENTER);

        // Panneau des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        // Recherche en temps réel
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterTable();
            }
        });

        // Boutons
        addButton.addActionListener(e -> addBook());
        editButton.addActionListener(e -> editBook());
        deleteButton.addActionListener(e -> deleteBook());
        refreshButton.addActionListener(e -> refresh());

        // Double-clic pour modifier
        booksTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editBook();
                }
            }
        });

        // Sélection de ligne
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = booksTable.getSelectedRow() != -1;
                editButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
            }
        });

        // État initial des boutons
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void filterTable() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void addBook() {
        BookDialog dialog = new BookDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Book book = dialog.getBook();
            try {
                bookDAO.save(book);
                refresh();
                showSuccessMessage("Livre ajouté avec succès!");
            } catch (Exception e) {
                e.printStackTrace(); // Debug
                showErrorMessage("Erreur lors de l'ajout du livre: " + e.getMessage());
            }
        }
    }

    private void editBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = booksTable.convertRowIndexToModel(selectedRow);
        int bookId = (Integer) tableModel.getValueAt(modelRow, 0);

        try {
            Book book = bookDAO.findById(bookId).orElse(null);
            if (book == null) {
                showErrorMessage("Livre non trouvé!");
                return;
            }

            BookDialog dialog = new BookDialog((Frame) SwingUtilities.getWindowAncestor(this), book);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                Book updatedBook = dialog.getBook();
                bookDAO.save(updatedBook);
                refresh();
                showSuccessMessage("Livre modifié avec succès!");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Debug
            showErrorMessage("Erreur lors de la modification du livre: " + e.getMessage());
        }
    }

    private void deleteBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = booksTable.convertRowIndexToModel(selectedRow);
        int bookId = (Integer) tableModel.getValueAt(modelRow, 0);
        String bookTitle = (String) tableModel.getValueAt(modelRow, 1);

        int option = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer le livre \"" + bookTitle + "\" ?",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            try {
                bookDAO.deleteById(bookId);
                refresh();
                showSuccessMessage("Livre supprimé avec succès!");
            } catch (Exception e) {
                e.printStackTrace(); // Debug
                showErrorMessage("Erreur lors de la suppression du livre: " + e.getMessage());
            }
        }
    }

    public void refresh() {
        loadBooks();
    }

    private void loadBooks() {
        System.out.println("Début du chargement des livres..."); // Debug
        
        SwingWorker<List<Book>, Void> worker = new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() throws Exception {
                try {
                    System.out.println("Appel de bookDAO.findAll()..."); // Debug
                    List<Book> books = bookDAO.findAll();
                    System.out.println("Livres récupérés: " + books.size()); // Debug
                    return books;
                } catch (Exception e) {
                    System.err.println("Erreur dans doInBackground: " + e.getMessage()); // Debug
                    e.printStackTrace();
                    throw e;
                }
            }

            @Override
            protected void done() {
                try {
                    List<Book> books = get();
                    System.out.println("Mise à jour de la table avec " + books.size() + " livres"); // Debug
                    updateTable(books);
                } catch (Exception e) {
                    System.err.println("Erreur dans done(): " + e.getMessage()); // Debug
                    e.printStackTrace();
                    showErrorMessage("Erreur lors du chargement des livres: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateTable(List<Book> books) {
        try {
            System.out.println("Début de updateTable avec " + books.size() + " livres"); // Debug
            tableModel.setRowCount(0);

            for (Book book : books) {
                Object[] row = {
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn(),
                    book.getPublisher(),
                    book.getPublicationYear(),
                    book.getCategory(),
                    book.getTotalCopies(),
                    book.getAvailableCopies(),
                    book.getLocation()
                };
                tableModel.addRow(row);
            }
            System.out.println("Table mise à jour avec succès"); // Debug
        } catch (Exception e) {
            System.err.println("Erreur dans updateTable: " + e.getMessage()); // Debug
            e.printStackTrace();
        }
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
