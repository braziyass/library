package com.bibliotheque;

import com.bibliotheque.ui.LoginDialog;
import com.bibliotheque.ui.MainWindow;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Classe principale de l'application de gestion de bibliothèque
 */
public class BibliothequeApp {
    private static final Logger LOGGER = Logger.getLogger(BibliothequeApp.class.getName());

    public static void main(String[] args) {
        // Configuration du Look and Feel
        setupLookAndFeel();
        
        // Configuration des propriétés système
        setupSystemProperties();
        
        // Démarrage de l'application
        SwingUtilities.invokeLater(BibliothequeApp::startApplication);
    }

    private static void setupLookAndFeel() {
        try {
            // Utiliser FlatLaf pour un look moderne
            UIManager.setLookAndFeel(new FlatLightLaf());
            
            // Configuration supplémentaire pour FlatLaf
            System.setProperty("flatlaf.useWindowDecorations", "true");
            System.setProperty("flatlaf.menuBarEmbedded", "true");
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Impossible de définir le Look and Feel FlatLaf, utilisation du défaut", e);
            try {
                // Fallback vers le Look and Feel système
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Impossible de définir le Look and Feel système", ex);
            }
        }
    }

    private static void setupSystemProperties() {
        // Configuration pour une meilleure qualité de rendu
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // Configuration pour les écrans haute résolution
        System.setProperty("sun.java2d.uiScale", "1.0");
        
        // Configuration de l'encodage
        System.setProperty("file.encoding", "UTF-8");
    }

    private static void startApplication() {
        try {
            // Afficher un splash screen (optionnel)
            showSplashScreen();
            
            // Afficher le dialog de connexion
            LoginDialog loginDialog = new LoginDialog(null);
            loginDialog.setVisible(true);
            
            // Vérifier si l'utilisateur s'est connecté
            if (loginDialog.isAuthenticated()) {
                String username = loginDialog.getAuthenticatedUser();
                LOGGER.info("Utilisateur connecté: " + username);
                
                // Créer et afficher la fenêtre principale
                MainWindow mainWindow = new MainWindow(username);
                mainWindow.showWindow();
                
            } else {
                LOGGER.info("Connexion annulée par l'utilisateur");
                System.exit(0);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du démarrage de l'application", e);
            showErrorDialog("Erreur de démarrage", 
                "Une erreur est survenue lors du démarrage de l'application:\n" + e.getMessage());
            System.exit(1);
        }
    }

    private static void showSplashScreen() {
        // Créer un splash screen simple
        JWindow splash = new JWindow();
        splash.setSize(400, 250);
        splash.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Logo/Titre
        JLabel titleLabel = new JLabel("Gestion de Bibliothèque");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel versionLabel = new JLabel("Version 1.0.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(Color.GRAY);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel loadingLabel = new JLabel("Chargement en cours...");
        loadingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        loadingLabel.setForeground(Color.GRAY);
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Barre de progression
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setMaximumSize(new Dimension(300, 20));
        
        // Ajouter les composants
        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(versionLabel);
        panel.add(Box.createVerticalStrut(30));
        panel.add(loadingLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(progressBar);
        panel.add(Box.createVerticalGlue());
        
        splash.add(panel);
        splash.setVisible(true);
        
        // Simuler le chargement
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        splash.dispose();
    }

    private static void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }
}