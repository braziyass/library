package com.bibliotheque.ui.dialogs;

import com.bibliotheque.model.Member;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MemberDialog extends JDialog {
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextArea addressArea;
    private JComboBox<String> statusComboBox;
    private JButton saveButton;
    private JButton cancelButton;
    
    private Member member;
    private boolean confirmed = false;

    public MemberDialog(Frame parent, Member member) {
        super(parent, member == null ? "Ajouter un membre" : "Modifier le membre", true);
        this.member = member != null ? member : new Member();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupDialog();
        populateFields();
    }

    private void initializeComponents() {
        // Champs de saisie
        firstNameField = new JTextField(20);
        firstNameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        firstNameField.setPreferredSize(new Dimension(250, 35));

        lastNameField = new JTextField(20);
        lastNameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lastNameField.setPreferredSize(new Dimension(250, 35));

        emailField = new JTextField(20);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailField.setPreferredSize(new Dimension(250, 35));

        phoneField = new JTextField(20);
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phoneField.setPreferredSize(new Dimension(250, 35));

        addressArea = new JTextArea(3, 20);
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);

        String[] statuses = {"active", "inactive", "suspended"};
        statusComboBox = new JComboBox<>(statuses);
        statusComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusComboBox.setPreferredSize(new Dimension(250, 35));

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
        JLabel titleLabel = new JLabel(member.getId() == 0 ? "Nouveau membre" : "Modifier le membre");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(70, 130, 180));

        // Panneau de formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setMaximumSize(new Dimension(400, 400));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // Prénom
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        JLabel firstNameLabel = new JLabel("Prénom *:");
        firstNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(firstNameLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(firstNameField, gbc);

        // Nom
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        JLabel lastNameLabel = new JLabel("Nom *:");
        lastNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(lastNameLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(lastNameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(emailLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(emailField, gbc);

        // Téléphone
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        JLabel phoneLabel = new JLabel("Téléphone:");
        phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(phoneLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(phoneField, gbc);

        // Adresse
        gbc.gridx = 0; gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE;
        JLabel addressLabel = new JLabel("Adresse:");
        addressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(addressLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 9; gbc.fill = GridBagConstraints.BOTH;
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setPreferredSize(new Dimension(250, 80));
        formPanel.add(addressScroll, gbc);

        // Statut
        gbc.gridx = 0; gbc.gridy = 10; gbc.fill = GridBagConstraints.NONE;
        JLabel statusLabel = new JLabel("Statut:");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(statusLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 11; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(statusComboBox, gbc);

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
        setSize(450, 600);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
    }

    private void populateFields() {
        if (member.getId() != 0) {
            firstNameField.setText(member.getFirstName());
            lastNameField.setText(member.getLastName());
            emailField.setText(member.getEmail());
            phoneField.setText(member.getPhone());
            addressArea.setText(member.getAddress());
            statusComboBox.setSelectedItem(member.getStatus());
        }
    }

    private class SaveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!validateFields()) {
                return;
            }

            // Mettre à jour l'objet member
            member.setFirstName(firstNameField.getText().trim());
            member.setLastName(lastNameField.getText().trim());
            member.setEmail(emailField.getText().trim());
            member.setPhone(phoneField.getText().trim());
            member.setAddress(addressArea.getText().trim());
            member.setStatus((String) statusComboBox.getSelectedItem());

            confirmed = true;
            dispose();
        }
    }

    private boolean validateFields() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();

        if (firstName.isEmpty()) {
            showErrorMessage("Le prénom est obligatoire.");
            firstNameField.requestFocus();
            return false;
        }

        if (lastName.isEmpty()) {
            showErrorMessage("Le nom est obligatoire.");
            lastNameField.requestFocus();
            return false;
        }

        if (!email.isEmpty() && (!email.contains("@") || !email.contains("."))) {
            showErrorMessage("Veuillez saisir une adresse email valide.");
            emailField.requestFocus();
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

    public Member getMember() {
        return member;
    }
}