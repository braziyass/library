package com.bibliotheque;

import com.bibliotheque.ui.LoginDialog;
import com.bibliotheque.ui.RegisterDialog;
import com.bibliotheque.ui.MainWindow;
import com.bibliotheque.dao.DatabaseManager;
import com.bibliotheque.utils.DatabaseRepair;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Classe principale de l'application de gestion de bibliothèque
 * Version avec système de registration
 */
public class BibliothequeApp {
    private static final Logger LOGGER = Logger.getLogger(BibliothequeApp.class.getName());
    private static final String APP_VERSION = "1.0.0";
    private static final String APP_TITLE = "Gestion de Bibliothèque";

    public static void main(String[] args) {
        // Configuration du système de logging
        setupLogging();
        
        // Configuration du Look and Feel
        setupLookAndFeel();
        
        // Configuration des propriétés système
        setupSystemProperties();
        
        // Démarrage de l'application
        SwingUtilities.invokeLater(BibliothequeApp::startApplication);
    }

    private static void setupLogging() {
        System.setProperty("java.util.logging.SimpleFormatter.format", 
            "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        LOGGER.info("Démarrage de l'application " + APP_TITLE + " v" + APP_VERSION);
    }

    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            System.setProperty("flatlaf.useWindowDecorations", "true");
            System.setProperty("flatlaf.menuBarEmbedded", "true");
            LOGGER.info("Look and Feel FlatLaf configuré avec succès");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Impossible de définir le Look and Feel FlatLaf, utilisation du défaut", e);
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
                LOGGER.info("Look and Feel système utilisé en fallback");
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Impossible de définir le Look and Feel système", ex);
            }
        }
    }

    private static void setupSystemProperties() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.uiScale", "1.0");
        System.setProperty("file.encoding", "UTF-8");
        LOGGER.info("Propriétés système configurées");
    }

    private static void startApplication() {
        try {
            // Initialiser et vérifier la base de données
            if (!initializeDatabase()) {
                showErrorDialog("Erreur de base de données", 
                    "Impossible d'initialiser la base de données.\nL'application ne peut pas démarrer.");
                System.exit(1);
                return;
            }
            
            // Afficher un splash screen
            showSplashScreen();
            
            // Démarrer le processus d'authentification
            startAuthenticationFlow();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur critique lors du démarrage de l'application", e);
            showErrorDialog("Erreur de démarrage", 
                "Une erreur critique est survenue lors du démarrage de l'application:\n\n" + 
                e.getMessage() + "\n\nVeuillez consulter les logs pour plus de détails.");
            cleanupAndExit(1);
        }
    }

    private static void startAuthenticationFlow() {
        // Créer une frame temporaire pour les dialogs
        JFrame tempFrame = new JFrame();
        tempFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        try {
            // Étape 1: Afficher le dialog de connexion
            LoginDialog loginDialog = new LoginDialog(tempFrame);
            loginDialog.setVisible(true);
            
            if (loginDialog.isAuthenticated()) {
                // Connexion réussie
                String username = loginDialog.getAuthenticatedUser();
                LOGGER.info("Utilisateur connecté: " + username);
                
                // Créer et afficher la fenêtre principale
                MainWindow mainWindow = new MainWindow(username);
                mainWindow.showWindow();
                
                LOGGER.info("Application démarrée avec succès pour l'utilisateur: " + username);
                
            } else {
                // Connexion échouée ou annulée - proposer l'inscription
                LOGGER.info("Connexion échouée, proposition d'inscription");
                
                int choice = JOptionPane.showConfirmDialog(
                    tempFrame,
                    "Connexion échouée ou annulée.\n\nVoulez-vous créer un nouveau compte ?",
                    "Inscription",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (choice == JOptionPane.YES_OPTION) {
                    // Afficher le dialog d'inscription
                    RegisterDialog registerDialog = new RegisterDialog(tempFrame);
                    registerDialog.setVisible(true);
                    
                    if (registerDialog.isRegistered()) {
                        // Inscription réussie - proposer une nouvelle connexion
                        LOGGER.info("Inscription réussie, nouvelle tentative de connexion");
                        
                        JOptionPane.showMessageDialog(
                            tempFrame,
                            "Inscription réussie !\n\nVous pouvez maintenant vous connecter avec vos nouveaux identifiants.",
                            "Inscription réussie",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        
                        // Nouvelle tentative de connexion
                        LoginDialog newLoginDialog = new LoginDialog(tempFrame);
                        newLoginDialog.setVisible(true);
                        
                        if (newLoginDialog.isAuthenticated()) {
                            String username = newLoginDialog.getAuthenticatedUser();
                            LOGGER.info("Utilisateur connecté après inscription: " + username);
                            
                            MainWindow mainWindow = new MainWindow(username);
                            mainWindow.showWindow();
                            
                        } else {
                            LOGGER.info("Connexion annulée après inscription");
                            cleanupAndExit(0);
                        }
                        
                    } else {
                        // Inscription annulée
                        LOGGER.info("Inscription annulée par l'utilisateur");
                        cleanupAndExit(0);
                    }
                    
                } else {
                    // L'utilisateur ne veut pas s'inscrire
                    LOGGER.info("Inscription refusée par l'utilisateur");
                    cleanupAndExit(0);
                }
            }
            
        } finally {
            // Nettoyer la frame temporaire
            tempFrame.dispose();
        }
    }

    private static boolean initializeDatabase() {
        try {
            LOGGER.info("Initialisation de la base de données...");
            
            DatabaseManager dbManager = DatabaseManager.getInstance();
            if (!dbManager.testConnection()) {
                LOGGER.severe("Impossible de se connecter à la base de données");
                return false;
            }
            
            DatabaseRepair.repairDatabase();
            LOGGER.info("Base de données initialisée avec succès");
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'initialisation de la base de données", e);
            
            try {
                LOGGER.info("Tentative de réparation d'urgence de la base de données...");
                DatabaseRepair.repairDatabase();
                return true;
            } catch (Exception repairException) {
                LOGGER.log(Level.SEVERE, "Échec de la réparation d'urgence", repairException);
                return false;
            }
        }
    }

    private static void showSplashScreen() {
        LOGGER.info("Affichage du splash screen");
        
        JWindow splash = new JWindow();
        splash.setSize(450, 300);
        splash.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel();
        panel.setBackground(new Color(245, 250, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 3),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Logo/Titre principal
        JLabel titleLabel = new JLabel(APP_TITLE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Sous-titre
        JLabel subtitleLabel = new JLabel("Système de Gestion Moderne");
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Version
        JLabel versionLabel = new JLabel("Version " + APP_VERSION);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(Color.GRAY);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Message de chargement
        JLabel loadingLabel = new JLabel("Initialisation en cours...");
        loadingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        loadingLabel.setForeground(new Color(70, 130, 180));
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Barre de progression
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setMaximumSize(new Dimension(350, 8));
        progressBar.setPreferredSize(new Dimension(350, 8));
        progressBar.setBackground(new Color(230, 230, 230));
        progressBar.setForeground(new Color(70, 130, 180));
        progressBar.setBorderPainted(false);
        
        // Assemblage du panneau
        panel.add(Box.createVerticalGlue());
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(versionLabel);
        panel.add(Box.createVerticalStrut(40));
        panel.add(loadingLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(progressBar);
        panel.add(Box.createVerticalGlue());
        
        splash.add(panel);
        splash.setVisible(true);
        
        // Simuler le chargement
        try {
            SwingUtilities.invokeLater(() -> loadingLabel.setText("Chargement des composants..."));
            Thread.sleep(800);
            
            SwingUtilities.invokeLater(() -> loadingLabel.setText("Vérification de la base de données..."));
            Thread.sleep(600);
            
            SwingUtilities.invokeLater(() -> loadingLabel.setText("Finalisation..."));
            Thread.sleep(400);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warning("Splash screen interrompu");
        }
        
        splash.dispose();
        LOGGER.info("Splash screen fermé");
    }

    private static void showErrorDialog(String title, String message) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel iconLabel = new JLabel("⚠️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageArea.setEditable(false);
        messageArea.setOpaque(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        
        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(messageArea, BorderLayout.CENTER);
        
        JOptionPane.showMessageDialog(null, panel, title, JOptionPane.ERROR_MESSAGE);
    }

    private static void cleanupAndExit(int exitCode) {
        try {
            LOGGER.info("Nettoyage des ressources avant fermeture...");
            DatabaseManager.getInstance().shutdown();
            LOGGER.info("Nettoyage terminé. Fermeture de l'application avec le code: " + exitCode);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors du nettoyage", e);
        } finally {
            System.exit(exitCode);
        }
    }

    public static String getVersion() {
        return APP_VERSION;
    }

    public static String getAppTitle() {
        return APP_TITLE;
    }
}
