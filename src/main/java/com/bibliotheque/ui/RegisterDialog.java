package com.bibliotheque.ui;

import com.bibliotheque.dao.UserDAO;
import com.bibliotheque.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterDialog extends JDialog {
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> roleComboBox;
    private JButton registerButton;
    private JButton cancelButton;
    private UserDAO userDAO;
    private boolean registered = false;
    private String registeredUsername;

    public RegisterDialog(Dialog parent) {
        super(parent, "Créer un compte", true);
        this.userDAO = new UserDAO();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupDialog();
    }

    private void initializeComponents() {
        // Champs de saisie
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usernameField.setPreferredSize(new Dimension(280, 35));

        emailField = new JTextField(20);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailField.setPreferredSize(new Dimension(280, 35));

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordField.setPreferredSize(new Dimension(280, 35));

        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        confirmPasswordField.setPreferredSize(new Dimension(280, 35));

        // ComboBox pour le rôle
        String[] roles = {"librarian", "admin"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        roleComboBox.setPreferredSize(new Dimension(280, 35));

        // Boutons
        registerButton = new JButton("Créer le compte");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerButton.setPreferredSize(new Dimension(140, 40));
        registerButton.setBackground(new Color(34, 139, 34));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);

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
        JLabel titleLabel = new JLabel("Créer un nouveau compte");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(34, 139, 34));

        JLabel subtitleLabel = new JLabel("Remplissez tous les champs pour créer votre compte");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(Color.GRAY);

        // Panneau de formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setMaximumSize(new Dimension(400, 350));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // Nom d'utilisateur
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        JLabel usernameLabel = new JLabel("Nom d'utilisateur *:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(usernameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        JLabel emailLabel = new JLabel("Email *:");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(emailLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(emailField, gbc);

        // Mot de passe
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        JLabel passwordLabel = new JLabel("Mot de passe *:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(passwordField, gbc);

        // Confirmer mot de passe
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        JLabel confirmPasswordLabel = new JLabel("Confirmer le mot de passe *:");
        confirmPasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(confirmPasswordLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(confirmPasswordField, gbc);

        // Rôle
        gbc.gridx = 0; gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE;
        JLabel roleLabel = new JLabel("Rôle:");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(roleLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 9; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(roleComboBox, gbc);

        // Panneau de boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        // Assemblage du panneau principal
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(25));
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(25));
        mainPanel.add(buttonPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        registerButton.addActionListener(new RegisterActionListener());
        cancelButton.addActionListener(e -> dispose());
    }

    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
    }

    private class RegisterActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!validateFields()) {
                return;
            }

            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();

            // Désactiver les boutons pendant l'inscription
            registerButton.setEnabled(false);
            cancelButton.setEnabled(false);
            registerButton.setText("Création...");

            // Inscription en arrière-plan
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    User newUser = new User(username, email, password, role);
                    userDAO.save(newUser);
                    return true;
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        
                        if (success) {
                            registered = true;
                            registeredUsername = username;
                            dispose();
                        }
                    } catch (Exception ex) {
                        String errorMessage = ex.getMessage();
                        if (errorMessage.contains("UNIQUE constraint failed")) {
                            if (errorMessage.contains("username")) {
                                showErrorMessage("Ce nom d'utilisateur existe déjà. Veuillez en choisir un autre.");
                            } else if (errorMessage.contains("email")) {
                                showErrorMessage("Cette adresse email est déjà utilisée. Veuillez en choisir une autre.");
                            } else {
                                showErrorMessage("Un compte avec ces informations existe déjà.");
                            }
                        } else {
                            showErrorMessage("Erreur lors de la création du compte: " + errorMessage);
                        }
                    } finally {
                        registerButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                        registerButton.setText("Créer le compte");
                    }
                }
            };

            worker.execute();
        }
    }

    private boolean validateFields() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty()) {
            showErrorMessage("Le nom d'utilisateur est obligatoire.");
            usernameField.requestFocus();
            return false;
        }

        if (username.length() < 3) {
            showErrorMessage("Le nom d'utilisateur doit contenir au moins 3 caractères.");
            usernameField.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            showErrorMessage("L'email est obligatoire.");
            emailField.requestFocus();
            return false;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showErrorMessage("Veuillez saisir une adresse email valide.");
            emailField.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            showErrorMessage("Le mot de passe est obligatoire.");
            passwordField.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            showErrorMessage("Le mot de passe doit contenir au moins 6 caractères.");
            passwordField.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showErrorMessage("Les mots de passe ne correspondent pas.");
            confirmPasswordField.requestFocus();
            return false;
        }

        return true;
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isRegistered() {
        return registered;
    }

    public String getRegisteredUsername() {
        return registeredUsername;
    }
}