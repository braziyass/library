package com.bibliotheque.ui;

import com.bibliotheque.dao.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RegisterDialog extends JDialog {
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton cancelButton;
    private JLabel messageLabel;
    
    private boolean registered = false;

    public RegisterDialog(JFrame parent) {
        super(parent, "Inscription - Gestion de Bibliothèque", true);
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupDialog();
    }

    private void initializeComponents() {
        // Champs de saisie
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(250, 35));

        emailField = new JTextField(20);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setPreferredSize(new Dimension(250, 35));

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(250, 35));

        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        confirmPasswordField.setPreferredSize(new Dimension(250, 35));

        // Boutons
        registerButton = new JButton("S'inscrire");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerButton.setPreferredSize(new Dimension(120, 40));
        registerButton.setBackground(new Color(34, 139, 34));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);

        cancelButton = new JButton("Annuler");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelButton.setPreferredSize(new Dimension(120, 40));

        // Message d'erreur
        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(Color.RED);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Panneau principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        mainPanel.setBackground(Color.WHITE);

        // Titre
        JLabel titleLabel = new JLabel("Créer un compte");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(34, 139, 34));

        JLabel subtitleLabel = new JLabel("Système de Gestion de Bibliothèque");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(Color.GRAY);

        // Panneau de formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setMaximumSize(new Dimension(350, 300));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);

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

        // Confirmation mot de passe
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        JLabel confirmLabel = new JLabel("Confirmer le mot de passe *:");
        confirmLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(confirmLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(confirmPasswordField, gbc);

        // Panneau de boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        // Assemblage du panneau principal
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(messageLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        // Action d'inscription
        ActionListener registerAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performRegistration();
            }
        };

        registerButton.addActionListener(registerAction);

        // Touche Entrée pour s'inscrire
        KeyListener enterKeyListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performRegistration();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {}
        };

        usernameField.addKeyListener(enterKeyListener);
        emailField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
        confirmPasswordField.addKeyListener(enterKeyListener);

        // Action d'annulation
        cancelButton.addActionListener(e -> {
            registered = false;
            dispose();
        });

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void setupDialog() {
        setSize(450, 600);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        // Focus initial sur le champ nom d'utilisateur
        SwingUtilities.invokeLater(() -> usernameField.requestFocus());
    }

    private void performRegistration() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Effacer le message précédent
        messageLabel.setText(" ");

        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Tous les champs marqués d'un * sont obligatoires.");
            return;
        }

        if (username.length() < 3) {
            showMessage("Le nom d'utilisateur doit contenir au moins 3 caractères.");
            usernameField.requestFocus();
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showMessage("Veuillez saisir une adresse email valide.");
            emailField.requestFocus();
            return;
        }

        if (password.length() < 4) {
            showMessage("Le mot de passe doit contenir au moins 4 caractères.");
            passwordField.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("Les mots de passe ne correspondent pas.");
            confirmPasswordField.setText("");
            confirmPasswordField.requestFocus();
            return;
        }

        // Désactiver le bouton pendant l'inscription
        registerButton.setEnabled(false);
        registerButton.setText("Inscription...");

        // Effectuer l'inscription en arrière-plan
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return registerUser(username, email, password);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        registered = true;
                        dispose();
                    } else {
                        showMessage("Ce nom d'utilisateur ou cette adresse email existe déjà.");
                        usernameField.requestFocus();
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'inscription: " + e.getMessage());
                    e.printStackTrace();
                    showMessage("Erreur lors de l'inscription. Veuillez réessayer.");
                } finally {
                    // Réactiver le bouton
                    registerButton.setEnabled(true);
                    registerButton.setText("S'inscrire");
                }
            }
        };
        worker.execute();
    }

    /**
     * VRAIE IMPLÉMENTATION - Enregistre l'utilisateur en base de données
     */
    private boolean registerUser(String username, String email, String password) {
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet rs = null;

        try {
            System.out.println("🔄 Début de l'inscription pour: " + username);
            
            // Obtenir la connexion à la base de données
            conn = DatabaseManager.getInstance().getConnection();
            
            // 1. Vérifier si l'utilisateur ou l'email existe déjà
            String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?";
            checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            checkStmt.setString(2, email);
            
            rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            
            if (count > 0) {
                System.out.println("❌ Utilisateur ou email déjà existant");
                return false;
            }
            
            // 2. Hasher le mot de passe
            String hashedPassword = hashPassword(password);
            
            // 3. Insérer le nouvel utilisateur
            String insertQuery = """
                INSERT INTO users (username, email, password_hash, role, created_at) 
                VALUES (?, ?, ?, 'librarian', ?)
                """;
            
            insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, username);
            insertStmt.setString(2, email);
            insertStmt.setString(3, hashedPassword);
            insertStmt.setString(4, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            int rowsAffected = insertStmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Utilisateur inscrit avec succès: " + username);
                System.out.println("📧 Email: " + email);
                System.out.println("🔐 Mot de passe hashé: " + hashedPassword.substring(0, 10) + "...");
                return true;
            } else {
                System.out.println("❌ Échec de l'insertion en base");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de l'inscription:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur générale lors de l'inscription:");
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            return false;
            
        } finally {
            // Fermer toutes les ressources
            DatabaseManager.safeClose(rs);
            DatabaseManager.safeClose(checkStmt);
            DatabaseManager.safeClose(insertStmt);
            DatabaseManager.safeClose(conn);
        }
    }

    /**
     * Hashe le mot de passe avec SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            // Ajouter un salt pour plus de sécurité
            String saltedPassword = password + "bibliotheque_salt_2024";
            byte[] hashedBytes = md.digest(saltedPassword.getBytes("UTF-8"));
            
            // Convertir en hexadécimal
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
            
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            System.err.println("Erreur lors du hashage du mot de passe: " + e.getMessage());
            // Fallback simple (pas recommandé en production)
            return "simple_" + password.hashCode();
        }
    }

    /**
     * Vérifie si un mot de passe correspond au hash stocké
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + "bibliotheque_salt_2024";
            byte[] hashedBytes = md.digest(saltedPassword.getBytes("UTF-8"));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString().equals(storedHash);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification du mot de passe: " + e.getMessage());
            return false;
        }
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        Timer timer = new Timer(7000, e -> messageLabel.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }

    // Getters
    public boolean isRegistered() {
        return registered;
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getEmail() {
        return emailField.getText().trim();
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }
}
