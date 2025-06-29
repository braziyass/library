package com.bibliotheque.ui.panels;

import com.bibliotheque.dao.LoanDAO;
import com.bibliotheque.model.Loan;
import com.bibliotheque.ui.dialogs.LoanDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LoansPanel extends JPanel {
    private LoanDAO loanDAO;
    private JTable loansTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton;
    private JButton returnButton;
    private JButton refreshButton;
    private JButton overdueButton;
    private TableRowSorter<DefaultTableModel> sorter;

    public LoansPanel() {
        this.loanDAO = new LoanDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadLoans();
    }

    private void initializeComponents() {
        // Champ de recherche
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(250, 35));

        // Boutons
        addButton = createButton("Nouvel emprunt", new Color(34, 139, 34), "➕");
        returnButton = createButton("Retourner", new Color(70, 130, 180), "↩️");
        overdueButton = createButton("Retards", new Color(220, 20, 60), "⚠️");
        refreshButton = createButton("Actualiser", new Color(128, 128, 128), "🔄");

        // Table
        String[] columnNames = {"ID", "Livre", "Auteur", "Membre", "Date d'emprunt", "Date d'échéance", "Date de retour", "Statut", "Notes"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        loansTable = new JTable(tableModel);
        loansTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loansTable.setRowHeight(25);
        loansTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loansTable.setGridColor(new Color(230, 230, 230));
        loansTable.setShowGrid(true);

        // Configuration des colonnes
        loansTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        loansTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Livre
        loansTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Auteur
        loansTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Membre
        loansTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Date emprunt
        loansTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Date échéance
        loansTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Date retour
        loansTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Statut
        loansTable.getColumnModel().getColumn(8).setPreferredWidth(150); // Notes

        // Tri
        sorter = new TableRowSorter<>(tableModel);
        loansTable.setRowSorter(sorter);

        // Masquer la colonne ID
        loansTable.getColumnModel().getColumn(0).setMinWidth(0);
        loansTable.getColumnModel().getColumn(0).setMaxWidth(0);
        loansTable.getColumnModel().getColumn(0).setWidth(0);
    }

    private JButton createButton(String text, Color color, String icon) {
        JButton button = new JButton(icon + " " + text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(140, 35));
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
        JLabel titleLabel = new JLabel("Gestion des Emprunts");
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
        JScrollPane scrollPane = new JScrollPane(loansTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Liste des emprunts"));
        add(scrollPane, BorderLayout.CENTER);

        // Panneau des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(returnButton);
        buttonPanel.add(overdueButton);
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
        addButton.addActionListener(e -> addLoan());
        returnButton.addActionListener(e -> returnBook());
        overdueButton.addActionListener(e -> showOverdueLoans());
        refreshButton.addActionListener(e -> refresh());

        // Double-clic pour voir les détails
        loansTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showLoanDetails();
                }
            }
        });

        // Sélection de ligne
        loansTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = loansTable.getSelectedRow() != -1;
                boolean isActive = hasSelection && isSelectedLoanActive();
                returnButton.setEnabled(isActive);
            }
        });

        // État initial des boutons
        returnButton.setEnabled(false);
    }

    private boolean isSelectedLoanActive() {
        int selectedRow = loansTable.getSelectedRow();
        if (selectedRow == -1) return false;
        
        int modelRow = loansTable.convertRowIndexToModel(selectedRow);
        String status = (String) tableModel.getValueAt(modelRow, 7);
        return "active".equals(status);
    }

    private void filterTable() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void addLoan() {
        LoanDialog dialog = new LoanDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Loan loan = dialog.getLoan();
            try {
                loanDAO.save(loan);
                refresh();
                showSuccessMessage("Emprunt créé avec succès!");
            } catch (Exception e) {
                showErrorMessage("Erreur lors de la création de l'emprunt: " + e.getMessage());
            }
        }
    }

    private void returnBook() {
        int selectedRow = loansTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = loansTable.convertRowIndexToModel(selectedRow);
        int loanId = (Integer) tableModel.getValueAt(modelRow, 0);
        String bookTitle = (String) tableModel.getValueAt(modelRow, 1);
        String memberName = (String) tableModel.getValueAt(modelRow, 3);

        int option = JOptionPane.showConfirmDialog(this,
                "Confirmer le retour du livre \"" + bookTitle + "\" par " + memberName + " ?",
                "Confirmation de retour",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            try {
                loanDAO.returnBook(loanId);
                refresh();
                showSuccessMessage("Livre retourné avec succès!");
            } catch (Exception e) {
                showErrorMessage("Erreur lors du retour du livre: " + e.getMessage());
            }
        }
    }

    private void showOverdueLoans() {
        SwingWorker<List<Loan>, Void> worker = new SwingWorker<List<Loan>, Void>() {
            @Override
            protected List<Loan> doInBackground() throws Exception {
                return loanDAO.findOverdueLoans();
            }

            @Override
            protected void done() {
                try {
                    List<Loan> overdueLoans = get();
                    updateTable(overdueLoans);
                    
                    if (overdueLoans.isEmpty()) {
                        showSuccessMessage("Aucun emprunt en retard!");
                    } else {
                        showInfoMessage("Affichage de " + overdueLoans.size() + " emprunt(s) en retard.");
                    }
                } catch (Exception e) {
                    showErrorMessage("Erreur lors du chargement des emprunts en retard: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showLoanDetails() {
        int selectedRow = loansTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = loansTable.convertRowIndexToModel(selectedRow);
        String bookTitle = (String) tableModel.getValueAt(modelRow, 1);
        String author = (String) tableModel.getValueAt(modelRow, 2);
        String memberName = (String) tableModel.getValueAt(modelRow, 3);
        String loanDate = (String) tableModel.getValueAt(modelRow, 4);
        String dueDate = (String) tableModel.getValueAt(modelRow, 5);
        String returnDate = (String) tableModel.getValueAt(modelRow, 6);
        String status = (String) tableModel.getValueAt(modelRow, 7);
        String notes = (String) tableModel.getValueAt(modelRow, 8);

        String details = String.format("""
            <html>
            <h3>Détails de l'emprunt</h3>
            <p><b>Livre:</b> %s</p>
            <p><b>Auteur:</b> %s</p>
            <p><b>Membre:</b> %s</p>
            <p><b>Date d'emprunt:</b> %s</p>
            <p><b>Date d'échéance:</b> %s</p>
            <p><b>Date de retour:</b> %s</p>
            <p><b>Statut:</b> %s</p>
            <p><b>Notes:</b> %s</p>
            </html>
            """, 
            bookTitle, author, memberName, loanDate, dueDate, 
            returnDate != null ? returnDate : "Non retourné", 
            status, notes != null ? notes : "Aucune");

        JOptionPane.showMessageDialog(this, details, "Détails de l'emprunt", JOptionPane.INFORMATION_MESSAGE);
    }

    public void refresh() {
        loadLoans();
    }

    private void loadLoans() {
        SwingWorker<List<Loan>, Void> worker = new SwingWorker<List<Loan>, Void>() {
            @Override
            protected List<Loan> doInBackground() throws Exception {
                return loanDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<Loan> loans = get();
                    updateTable(loans);
                } catch (Exception e) {
                    showErrorMessage("Erreur lors du chargement des emprunts: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateTable(List<Loan> loans) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Loan loan : loans) {
            // Colorer les lignes en retard
            Object[] row = {
                loan.getId(),
                loan.getBookTitle(),
                loan.getBookAuthor(),
                loan.getMemberName(),
                loan.getLoanDate() != null ? loan.getLoanDate().format(formatter) : "",
                loan.getDueDate() != null ? loan.getDueDate().format(formatter) : "",
                loan.getReturnDate() != null ? loan.getReturnDate().format(formatter) : "",
                loan.getStatus(),
                loan.getNotes()
            };
            tableModel.addRow(row);
        }
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}