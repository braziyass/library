package com.bibliotheque.ui.dialogs;

import com.bibliotheque.dao.BookDAO;
import com.bibliotheque.dao.MemberDAO;
import com.bibliotheque.model.Book;
import com.bibliotheque.model.Loan;
import com.bibliotheque.model.Member;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;

public class LoanDialog extends JDialog {
    private JComboBox<Book> bookComboBox;
    private JComboBox<Member> memberComboBox;
    private JSpinner dueDaysSpinner;
    private JTextArea notesArea;
    private JButton saveButton;
    private JButton cancelButton;
    
    private BookDAO bookDAO;
    private MemberDAO memberDAO;
    private Loan loan;
    private boolean confirmed = false;

    public LoanDialog(Frame parent, Loan loan) {
        super(parent, "Nouvel emprunt", true);
        this.loan = loan != null ? loan : new Loan();
        this.bookDAO = new BookDAO();
        this.memberDAO = new MemberDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupDialog();
        loadData();
    }

    private void initializeComponents() {
        // ComboBox pour les livres
        bookComboBox = new JComboBox<>();
        bookComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bookComboBox.setPreferredSize(new Dimension(300, 35));
        bookComboBox.setRenderer(new BookComboBoxRenderer());

        // ComboBox pour les membres
        memberComboBox = new JComboBox<>();
        memberComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        memberComboBox.setPreferredSize(new Dimension(300, 35));
        memberComboBox.setRenderer(new MemberComboBoxRenderer());

        // Spinner pour les jours d'échéance
        dueDaysSpinner = new JSpinner(new SpinnerNumberModel(14, 1, 365, 1));
        dueDaysSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dueDaysSpinner.setPreferredSize(new Dimension(100, 35));

        // Zone de notes
        notesArea = new JTextArea(3, 25);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        // Boutons
        saveButton = new JButton("Créer l'emprunt");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.setPreferredSize(new Dimension(140, 40));
        saveButton.setBackground(new Color(34, 139, 34));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);

        cancelButton = new JButton("Annuler");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelButton.setPreferredSize(new Dimension(140, 40));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Panneau principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);

        // Titre
        JLabel titleLabel = new JLabel("Créer un nouvel emprunt");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(70, 130, 180));

        // Panneau de formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setMaximumSize(new Dimension(450, 350));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Livre
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        JLabel bookLabel = new JLabel("Livre *:");
        bookLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(bookLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(bookComboBox, gbc);

        // Membre
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        JLabel memberLabel = new JLabel("Membre *:");
        memberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(memberLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(memberComboBox, gbc);

        // Durée d'emprunt
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        JLabel daysLabel = new JLabel("Durée d'emprunt (jours):");
        daysLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(daysLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(dueDaysSpinner, gbc);

        // Notes
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        JLabel notesLabel = new JLabel("Notes:");
        notesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(notesLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.BOTH;
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(300, 80));
        formPanel.add(notesScroll, gbc);

        // Panneau de boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Assemblage du panneau principal
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(25));
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(25));
        mainPanel.add(buttonPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        saveButton.addActionListener(new SaveActionListener());
        cancelButton.addActionListener(e -> dispose());
    }

    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 550);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
    }

    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Charger les livres disponibles
                List<Book> availableBooks = bookDAO.findAvailableBooks();
                SwingUtilities.invokeLater(() -> {
                    for (Book book : availableBooks) {
                        bookComboBox.addItem(book);
                    }
                });

                // Charger les membres actifs
                List<Member> activeMembers = memberDAO.search(""); // Tous les membres
                SwingUtilities.invokeLater(() -> {
                    for (Member member : activeMembers) {
                        if ("active".equals(member.getStatus())) {
                            memberComboBox.addItem(member);
                        }
                    }
                });

                return null;
            }

            @Override
            protected void done() {
                saveButton.setEnabled(bookComboBox.getItemCount() > 0 && memberComboBox.getItemCount() > 0);
                if (bookComboBox.getItemCount() == 0) {
                    showErrorMessage("Aucun livre disponible pour l'emprunt.");
                }
                if (memberComboBox.getItemCount() == 0) {
                    showErrorMessage("Aucun membre actif trouvé.");
                }
            }
        };
        worker.execute();
    }

    private class SaveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!validateFields()) {
                return;
            }

            Book selectedBook = (Book) bookComboBox.getSelectedItem();
            Member selectedMember = (Member) memberComboBox.getSelectedItem();
            int dueDays = (Integer) dueDaysSpinner.getValue();

            // Mettre à jour l'objet loan
            loan.setBookId(selectedBook.getId());
            loan.setMemberId(selectedMember.getId());
            loan.setLoanDate(LocalDate.now());
            loan.setDueDate(LocalDate.now().plusDays(dueDays));
            loan.setNotes(notesArea.getText().trim());
            loan.setStatus("active");

            confirmed = true;
            dispose();
        }
    }

    private boolean validateFields() {
        if (bookComboBox.getSelectedItem() == null) {
            showErrorMessage("Veuillez sélectionner un livre.");
            return false;
        }

        if (memberComboBox.getSelectedItem() == null) {
            showErrorMessage("Veuillez sélectionner un membre.");
            return false;
        }

        return true;
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Loan getLoan() {
        return loan;
    }

    // Renderer personnalisé pour les livres
    private class BookComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Book) {
                Book book = (Book) value;
                setText(book.getTitle() + " - " + book.getAuthor() + " (Disponibles: " + book.getAvailableCopies() + ")");
            }
            
            return this;
        }
    }

    // Renderer personnalisé pour les membres
    private class MemberComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Member) {
                Member member = (Member) value;
                setText(member.getFullName() + " (" + member.getEmail() + ")");
            }
            
            return this;
        }
    }
}
