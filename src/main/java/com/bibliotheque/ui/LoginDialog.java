package com.bibliotheque.ui;

import com.bibliotheque.dao.UserDAO;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private UserDAO userDAO;
    private boolean authenticated = false;
    private String authenticatedUser;

    public LoginDialog(Frame parent) {
        super(parent, "Connexion - Gestion de Bibliothèque", true);
        this.userDAO = new UserDAO();
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
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loginButton.setPreferredSize(new Dimension(120, 35));
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

        cancelButton = new JButton("Annuler");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelButton.setPreferredSize(new Dimension(120, 35));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Panneau principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);

        // Titre
        JLabel titleLabel = new JLabel("Gestion de Bibliothèque");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(70, 130, 180));

        JLabel subtitleLabel = new JLabel("Connectez-vous pour accéder au système");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(Color.GRAY);

        // Panneau de formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // Nom d'utilisateur
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        JLabel usernameLabel = new JLabel("Nom d'utilisateur:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(usernameField, gbc);

        // Mot de passe
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        JLabel passwordLabel = new JLabel("Mot de passe:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(passwordField, gbc);

        // Panneau de boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);

        // Informations de test
        JLabel infoLabel = new JLabel("<html><center><b>Compte de test:</b><br>admin / admin123</center></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoLabel.setForeground(new Color(100, 100, 100));

        // Assemblage du panneau principal
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(infoLabel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        loginButton.addActionListener(new LoginActionListener());
        cancelButton.addActionListener(e -> {
            authenticated = false;
            dispose();
        });

        // Permettre la connexion avec Entrée
        KeyListener enterKeyListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginButton.doClick();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {}
        };

        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);

        // Focus initial sur le champ username
        SwingUtilities.invokeLater(() -> usernameField.requestFocus());
    }

    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 380);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
    }

    private class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                showErrorMessage("Veuillez saisir votre nom d'utilisateur et votre mot de passe.");
                return;
            }

            // Désactiver le bouton pendant l'authentification
            loginButton.setEnabled(false);
            loginButton.setText("Connexion...");

            // Authentification en arrière-plan
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return userDAO.authenticate(username, password);
                }

                @Override
                protected void done() {
                    try {
                        boolean isAuthenticated = get();
                        
                        if (isAuthenticated) {
                            authenticated = true;
                            authenticatedUser = username;
                            dispose();
                        } else {
                            showErrorMessage("Nom d'utilisateur ou mot de passe incorrect.");
                            passwordField.setText("");
                            passwordField.requestFocus();
                        }
                    } catch (Exception ex) {
                        showErrorMessage("Erreur lors de la connexion: " + ex.getMessage());
                    } finally {
                        loginButton.setEnabled(true);
                        loginButton.setText("Se connecter");
                    }
                }
            };

            worker.execute();
        }
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getAuthenticatedUser() {
        return authenticatedUser;
    }

    public static void main(String[] args) {
        // Test du dialog de connexion
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception e) {
                e.printStackTrace();
            }

            LoginDialog dialog = new LoginDialog(null);
            dialog.setVisible(true);

            if (dialog.isAuthenticated()) {
                System.out.println("Utilisateur connecté: " + dialog.getAuthenticatedUser());
            } else {
                System.out.println("Connexion annulée");
            }

            System.exit(0);
        });
    }
}