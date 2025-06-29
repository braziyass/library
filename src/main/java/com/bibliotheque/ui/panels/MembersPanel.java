package com.bibliotheque.ui.panels;

import com.bibliotheque.dao.MemberDAO;
import com.bibliotheque.model.Member;
import com.bibliotheque.ui.dialogs.MemberDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MembersPanel extends JPanel {
    private MemberDAO memberDAO;
    private JTable membersTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private TableRowSorter<DefaultTableModel> sorter;

    public MembersPanel() {
        this.memberDAO = new MemberDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadMembers();
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
        String[] columnNames = {"ID", "Prénom", "Nom", "Email", "Téléphone", "Adresse", "Date d'inscription", "Statut"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        membersTable = new JTable(tableModel);
        membersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        membersTable.setRowHeight(25);
        membersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        membersTable.setGridColor(new Color(230, 230, 230));
        membersTable.setShowGrid(true);

        // Configuration des colonnes
        membersTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        membersTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Prénom
        membersTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Nom
        membersTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Email
        membersTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Téléphone
        membersTable.getColumnModel().getColumn(5).setPreferredWidth(200); // Adresse
        membersTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Date
        membersTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Statut

        // Tri
        sorter = new TableRowSorter<>(tableModel);
        membersTable.setRowSorter(sorter);

        // Masquer la colonne ID
        membersTable.getColumnModel().getColumn(0).setMinWidth(0);
        membersTable.getColumnModel().getColumn(0).setMaxWidth(0);
        membersTable.getColumnModel().getColumn(0).setWidth(0);
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
        JLabel titleLabel = new JLabel("Gestion des Membres");
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
        JScrollPane scrollPane = new JScrollPane(membersTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Liste des membres"));
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
        addButton.addActionListener(e -> addMember());
        editButton.addActionListener(e -> editMember());
        deleteButton.addActionListener(e -> deleteMember());
        refreshButton.addActionListener(e -> refresh());

        // Double-clic pour modifier
        membersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editMember();
                }
            }
        });

        // Sélection de ligne
        membersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = membersTable.getSelectedRow() != -1;
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

    private void addMember() {
        MemberDialog dialog = new MemberDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Member member = dialog.getMember();
            try {
                memberDAO.save(member);
                refresh();
                showSuccessMessage("Membre ajouté avec succès!");
            } catch (Exception e) {
                showErrorMessage("Erreur lors de l'ajout du membre: " + e.getMessage());
            }
        }
    }

    private void editMember() {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = membersTable.convertRowIndexToModel(selectedRow);
        int memberId = (Integer) tableModel.getValueAt(modelRow, 0);

        try {
            Member member = memberDAO.findById(memberId).orElse(null);
            if (member == null) {
                showErrorMessage("Membre non trouvé!");
                return;
            }

            MemberDialog dialog = new MemberDialog((Frame) SwingUtilities.getWindowAncestor(this), member);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                Member updatedMember = dialog.getMember();
                memberDAO.save(updatedMember);
                refresh();
                showSuccessMessage("Membre modifié avec succès!");
            }
        } catch (Exception e) {
            showErrorMessage("Erreur lors de la modification du membre: " + e.getMessage());
        }
    }

    private void deleteMember() {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = membersTable.convertRowIndexToModel(selectedRow);
        int memberId = (Integer) tableModel.getValueAt(modelRow, 0);
        String memberName = tableModel.getValueAt(modelRow, 1) + " " + tableModel.getValueAt(modelRow, 2);

        int option = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer le membre \"" + memberName + "\" ?",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            try {
                memberDAO.deleteById(memberId);
                refresh();
                showSuccessMessage("Membre supprimé avec succès!");
            } catch (Exception e) {
                showErrorMessage("Erreur lors de la suppression du membre: " + e.getMessage());
            }
        }
    }

    public void refresh() {
        loadMembers();
    }

    private void loadMembers() {
        SwingWorker<List<Member>, Void> worker = new SwingWorker<List<Member>, Void>() {
            @Override
            protected List<Member> doInBackground() throws Exception {
                return memberDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<Member> members = get();
                    updateTable(members);
                } catch (Exception e) {
                    showErrorMessage("Erreur lors du chargement des membres: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateTable(List<Member> members) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Member member : members) {
            Object[] row = {
                member.getId(),
                member.getFirstName(),
                member.getLastName(),
                member.getEmail(),
                member.getPhone(),
                member.getAddress(),
                member.getMembershipDate() != null ? member.getMembershipDate().format(formatter) : "",
                member.getStatus()
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
}