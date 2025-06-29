package com.bibliotheque.ui.dialogs;

import com.bibliotheque.model.Book;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BookDialog extends JDialog {
    private JTextField titleField;
    private JTextField authorField;
    private JTextField isbnField;
    private JTextField publisherField;
    private JSpinner yearSpinner;
    private JTextField categoryField;
    private JSpinner totalCopiesSpinner;
    private JSpinner availableCopiesSpinner;
    private JTextField locationField;
    private JButton saveButton;
    private JButton cancelButton;
    
    private Book book;
    private boolean confirmed = false;

    public BookDialog(Frame parent, Book book) {
        super(parent, book == null ? "Ajouter un livre" : "Modifier le livre", true);
        this.book = book != null ? book : new Book();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupDialog();
        populateFields();
    }

    private void initializeComponents() {
        // Champs de saisie
        titleField = new JTextField(25);
        titleField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleField.setPreferredSize(new Dimension(400, 35));

        authorField = new JTextField(25);
        authorField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        authorField.setPreferredSize(new Dimension(400, 35));

        isbnField = new JTextField(25);
        isbnField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        isbnField.setPreferredSize(new Dimension(400, 35));

        publisherField = new JTextField(25);
        publisherField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        publisherField.setPreferredSize(new Dimension(400, 35));

        yearSpinner = new JSpinner(new SpinnerNumberModel(2024, 1000, 2030, 1));
        yearSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        yearSpinner.setPreferredSize(new Dimension(100, 35));

        categoryField = new JTextField(25);
        categoryField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryField.setPreferredSize(new Dimension(400, 35));

        totalCopiesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        totalCopiesSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        totalCopiesSpinner.setPreferredSize(new Dimension(100, 35));

        availableCopiesSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
        availableCopiesSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        availableCopiesSpinner.setPreferredSize(new Dimension(100, 35));

        locationField = new JTextField(25);
        locationField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        locationField.setPreferredSize(new Dimension(400, 35));

        // Boutons
        saveButton = new JButton("Enregistrer");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.setPreferredSize(new Dimension(120, 40));
        saveButton.setBackground(new Color(34, 139, 34));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);

        cancelButton = new JButton("Annuler");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelButton.setPreferredSize(new Dimension(120, 40));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Panneau principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);

        // Titre
        JLabel titleLabel = new JLabel(book.getId() == 0 ? "Nouveau livre" : "Modifier le livre");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(70, 130, 180));

        // Panneau de formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setMaximumSize(new Dimension(550, 600));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // Titre du livre
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        JLabel bookTitleLabel = new JLabel("Titre *:");
        bookTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(bookTitleLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(titleField, gbc);

        // Auteur
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        JLabel authorLabel = new JLabel("Auteur *:");
        authorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(authorLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(authorField, gbc);

        // ISBN
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        JLabel isbnLabel = new JLabel("ISBN:");
        isbnLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(isbnLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(isbnField, gbc);

        // Éditeur
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        JLabel publisherLabel = new JLabel("Éditeur:");
        publisherLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(publisherLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(publisherField, gbc);

        // Année de publication
        gbc.gridx = 0; gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE;
        JLabel yearLabel = new JLabel("Année de publication:");
        yearLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(yearLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 9; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(yearSpinner, gbc);

        // Catégorie
        gbc.gridx = 0; gbc.gridy = 10; gbc.fill = GridBagConstraints.NONE;
        JLabel categoryLabel = new JLabel("Catégorie:");
        categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(categoryLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 11; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(categoryField, gbc);

        // Nombre total d'exemplaires
        gbc.gridx = 0; gbc.gridy = 12; gbc.fill = GridBagConstraints.NONE;
        JLabel totalLabel = new JLabel("Nombre total d'exemplaires:");
        totalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(totalLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 13; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(totalCopiesSpinner, gbc);

        // Exemplaires disponibles
        gbc.gridx = 0; gbc.gridy = 14; gbc.fill = GridBagConstraints.NONE;
        JLabel availableLabel = new JLabel("Exemplaires disponibles:");
        availableLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(availableLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 15; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(availableCopiesSpinner, gbc);

        // Emplacement
        gbc.gridx = 0; gbc.gridy = 16; gbc.fill = GridBagConstraints.NONE;
        JLabel locationLabel = new JLabel("Emplacement:");
        locationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(locationLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 17; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(locationField, gbc);

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

        // Synchroniser les spinners
        totalCopiesSpinner.addChangeListener(e -> {
            int totalCopies = (Integer) totalCopiesSpinner.getValue();
            int availableCopies = (Integer) availableCopiesSpinner.getValue();
            if (availableCopies > totalCopies) {
                availableCopiesSpinner.setValue(totalCopies);
            }
            ((SpinnerNumberModel) availableCopiesSpinner.getModel()).setMaximum(totalCopies);
        });
    }

    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(650, 800);
        setLocationRelativeTo(getParent());
        setResizable(true);
        setMinimumSize(new Dimension(600, 700));
        getContentPane().setBackground(Color.WHITE);
    }

    private void populateFields() {
        if (book.getId() != 0) {
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            isbnField.setText(book.getIsbn());
            publisherField.setText(book.getPublisher());
            yearSpinner.setValue(book.getPublicationYear());
            categoryField.setText(book.getCategory());
            totalCopiesSpinner.setValue(book.getTotalCopies());
            availableCopiesSpinner.setValue(book.getAvailableCopies());
            locationField.setText(book.getLocation());
        }
    }

    private class SaveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!validateFields()) {
                return;
            }

            // Mettre à jour l'objet book
            book.setTitle(titleField.getText().trim());
            book.setAuthor(authorField.getText().trim());
            book.setIsbn(isbnField.getText().trim());
            book.setPublisher(publisherField.getText().trim());
            book.setPublicationYear((Integer) yearSpinner.getValue());
            book.setCategory(categoryField.getText().trim());
            book.setTotalCopies((Integer) totalCopiesSpinner.getValue());
            book.setAvailableCopies((Integer) availableCopiesSpinner.getValue());
            book.setLocation(locationField.getText().trim());

            confirmed = true;
            dispose();
        }
    }

    private boolean validateFields() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();

        if (title.isEmpty()) {
            showErrorMessage("Le titre est obligatoire.");
            titleField.requestFocus();
            return false;
        }

        if (author.isEmpty()) {
            showErrorMessage("L'auteur est obligatoire.");
            authorField.requestFocus();
            return false;
        }

        int totalCopies = (Integer) totalCopiesSpinner.getValue();
        int availableCopies = (Integer) availableCopiesSpinner.getValue();

        if (availableCopies > totalCopies) {
            showErrorMessage("Le nombre d'exemplaires disponibles ne peut pas être supérieur au nombre total.");
            availableCopiesSpinner.requestFocus();
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

    public Book getBook() {
        return book;
    }
}
