package com.bibliotheque.ui;

import com.bibliotheque.dao.DatabaseManager;
import com.bibliotheque.ui.RegisterDialog;

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

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private JLabel messageLabel;
    
    private boolean authenticated = false;
    private String authenticatedUser = null;

    public LoginDialog(Frame parent) {
        super(parent, "Connexion - Gestion de Bibliothèque", true);
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

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(250, 35));

        // Boutons
        loginButton = new JButton("Se connecter");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginButton.setPreferredSize(new Dimension(120, 40));
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

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
        JLabel titleLabel = new JLabel("Connexion");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(70, 130, 180));

        JLabel subtitleLabel = new JLabel("Système de Gestion de Bibliothèque");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(Color.GRAY);

        // Panneau de formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setMaximumSize(new Dimension(350, 200));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Nom d'utilisateur
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        JLabel usernameLabel = new JLabel("Nom d'utilisateur:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(usernameField, gbc);

        // Mot de passe
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        JLabel passwordLabel = new JLabel("Mot de passe:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(passwordField, gbc);

        // Panneau de boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);

        // Panneau d'informations
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(240, 248, 255));
        infoPanel.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setMaximumSize(new Dimension(350, 100));

        JLabel infoTitle = new JLabel("Comptes de test ou créez un nouveau compte:");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        infoTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel adminInfo = new JLabel("Admin: admin / admin");
        adminInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        adminInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel librarianInfo = new JLabel("Bibliothécaire: biblio / biblio");
        librarianInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        librarianInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(infoTitle);
        infoPanel.add(adminInfo);
        infoPanel.add(librarianInfo);
        infoPanel.add(Box.createVerticalStrut(5));

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
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(infoPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        // Action de connexion
        ActionListener loginAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        };

        loginButton.addActionListener(loginAction);

        // Touche Entrée pour se connecter
        KeyListener enterKeyListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {}
        };

        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);

        // Action d'annulation
        cancelButton.addActionListener(e -> {
            authenticated = false;
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

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Effacer le message précédent
        messageLabel.setText(" ");

        // Validation simple
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Veuillez saisir le nom d'utilisateur et le mot de passe.");
            return;
        }

        // Désactiver le bouton pendant l'authentification
        loginButton.setEnabled(false);
        loginButton.setText("Connexion...");

        // Effectuer l'authentification en arrière-plan
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return authenticateUser(username, password);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        authenticated = true;
                        authenticatedUser = username;
                        dispose();
                    } else {
                        showMessage("Nom d'utilisateur ou mot de passe incorrect.");
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'authentification: " + e.getMessage());
                    showMessage("Erreur de connexion. Veuillez réessayer.");
                } finally {
                    // Réactiver le bouton
                    loginButton.setEnabled(true);
                    loginButton.setText("Se connecter");
                }
            }
        };
        worker.execute();
    }

    /**
     * VRAIE AUTHENTIFICATION - Vérifie contre la base de données
     */
    private boolean authenticateUser(String username, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            System.out.println("🔄 Tentative de connexion pour: " + username);
            
            // Vérification des comptes de test d'abord
            if ((username.equals("admin") && password.equals("admin")) ||
                (username.equals("biblio") && password.equals("biblio")) ||
                (username.equals("bibliothecaire") && password.equals("bibliothecaire"))) {
                System.out.println("✅ Connexion réussie avec compte de test: " + username);
                return true;
            }
            
            // Vérification en base de données
            conn = DatabaseManager.getInstance().getConnection();
            
            String query = "SELECT username, password_hash FROM users WHERE username = ? OR email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, username); // Permettre la connexion avec email aussi
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedUsername = rs.getString("username");
                String storedHash = rs.getString("password_hash");
                
                // Vérifier le mot de passe
                if (RegisterDialog.verifyPassword(password, storedHash)) {
                    System.out.println("✅ Connexion réussie depuis la base: " + storedUsername);
                    return true;
                } else {
                    System.out.println("❌ Mot de passe incorrect pour: " + username);
                    return false;
                }
            } else {
                System.out.println("❌ Utilisateur non trouvé: " + username);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de l'authentification:");
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur générale lors de l'authentification:");
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            return false;
            
        } finally {
            DatabaseManager.safeClose(rs);
            DatabaseManager.safeClose(stmt);
            DatabaseManager.safeClose(conn);
        }
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        Timer timer = new Timer(5000, e -> messageLabel.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }

    // Getters
    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getAuthenticatedUser() {
        return authenticatedUser;
    }
}
