package com.bibliotheque.ui;

import com.bibliotheque.dao.DatabaseManager;
import com.bibliotheque.ui.panels.DashboardPanel;
import com.bibliotheque.ui.panels.BooksPanel;
import com.bibliotheque.ui.panels.MembersPanel;
import com.bibliotheque.ui.panels.LoansPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainWindow extends JFrame {
    private String currentUser;
    private JTabbedPane tabbedPane;
    private DashboardPanel dashboardPanel;
    private BooksPanel booksPanel;
    private MembersPanel membersPanel;
    private LoansPanel loansPanel;
    private Timer clockTimer;

    public MainWindow(String username) {
        this.currentUser = username;
        initializeComponents();
        setupLayout();
        setupMenuBar();
        setupEventHandlers();
        setupWindow();
    }

    private void initializeComponents() {
        // Créer le panneau à onglets
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Créer les panneaux
        dashboardPanel = new DashboardPanel();
        booksPanel = new BooksPanel();
        membersPanel = new MembersPanel();
        loansPanel = new LoansPanel();

        // Ajouter les onglets avec des icônes
        tabbedPane.addTab("Tableau de bord", createIcon("🏠"), dashboardPanel, "Vue d'ensemble de la bibliothèque");
        tabbedPane.addTab("Livres", createIcon("📚"), booksPanel, "Gestion des livres");
        tabbedPane.addTab("Membres", createIcon("👥"), membersPanel, "Gestion des membres");
        tabbedPane.addTab("Emprunts", createIcon("📋"), loansPanel, "Gestion des emprunts");
    }

    private Icon createIcon(String emoji) {
        // Créer une icône simple avec un emoji
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                g2.drawString(emoji, x, y + 14);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return 20; }

            @Override
            public int getIconHeight() { return 16; }
        };
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

        // Barre de statut
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setPreferredSize(new Dimension(0, 25));

        JLabel statusLabel = new JLabel(" Connecté en tant que: " + currentUser);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusBar.add(statusLabel, BorderLayout.WEST);

        JLabel timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusBar.add(timeLabel, BorderLayout.EAST);

        // Mettre à jour l'heure toutes les secondes
        clockTimer = new Timer(1000, e -> {
            timeLabel.setText(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + " ");
        });
        clockTimer.start();

        add(statusBar, BorderLayout.SOUTH);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Menu Fichier
        JMenu fileMenu = new JMenu("Fichier");
        fileMenu.setMnemonic('F');

        JMenuItem refreshItem = new JMenuItem("Actualiser");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        refreshItem.addActionListener(e -> refreshCurrentPanel());

        JMenuItem exitItem = new JMenuItem("Quitter");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        exitItem.addActionListener(e -> exitApplication());

        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Menu Outils
        JMenu toolsMenu = new JMenu("Outils");
        toolsMenu.setMnemonic('O');

        JMenuItem backupItem = new JMenuItem("Sauvegarder la base de données");
        backupItem.addActionListener(e -> backupDatabase());

        JMenuItem settingsItem = new JMenuItem("Paramètres");
        settingsItem.addActionListener(e -> showSettings());

        toolsMenu.add(backupItem);
        toolsMenu.addSeparator();
        toolsMenu.add(settingsItem);

        // Menu Aide
        JMenu helpMenu = new JMenu("Aide");
        helpMenu.setMnemonic('A');

        JMenuItem aboutItem = new JMenuItem("À propos");
        aboutItem.addActionListener(e -> showAbout());

        helpMenu.add(aboutItem);

        // Ajouter les menus à la barre de menu
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(Box.createHorizontalGlue()); // Pousser le menu Aide à droite
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void setupEventHandlers() {
        // Gestionnaire de fermeture de fenêtre
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        // Gestionnaire de changement d'onglet
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            refreshPanelAtIndex(selectedIndex);
        });
    }

    private void setupWindow() {
        setTitle("Gestion de Bibliothèque - " + currentUser);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));

        // Icône de l'application (optionnel)
        try {
            // Vous pouvez ajouter une icône personnalisée ici
            // setIconImage(ImageIO.read(getClass().getResource("/icon.png")));
        } catch (Exception e) {
            // Ignorer si l'icône n'est pas trouvée
        }
    }

    private void refreshCurrentPanel() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        refreshPanelAtIndex(selectedIndex);
    }

    private void refreshPanelAtIndex(int index) {
        try {
            switch (index) {
                case 0 -> {
                    if (dashboardPanel != null) {
                        dashboardPanel.refresh();
                    }
                }
                case 1 -> {
                    if (booksPanel != null) {
                        booksPanel.refresh();
                    }
                }
                case 2 -> {
                    if (membersPanel != null) {
                        membersPanel.refresh();
                    }
                }
                case 3 -> {
                    if (loansPanel != null) {
                        loansPanel.refresh();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du rafraîchissement du panneau: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Erreur lors du rafraîchissement: " + e.getMessage(), 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void backupDatabase() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Sauvegarder la base de données");
        fileChooser.setSelectedFile(new java.io.File("bibliotheque_backup_" +
            java.time.LocalDate.now().toString() + ".db"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Implémenter la sauvegarde
            JOptionPane.showMessageDialog(this,
                "Fonctionnalité de sauvegarde à implémenter",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showSettings() {
        JOptionPane.showMessageDialog(this,
            "Fenêtre de paramètres à implémenter",
            "Paramètres",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        String aboutText = """
            <html>
            <h2>Gestion de Bibliothèque</h2>
            <p><b>Version:</b> 1.0.0</p>
            <p><b>Développé avec:</b> Java Swing & SQLite</p>
            <p><b>Utilisateur connecté:</b> %s</p>
            <br>
            <p>Application de gestion complète pour bibliothèques</p>
            <p>© 2024 - Tous droits réservés</p>
            </html>
            """.formatted(currentUser);

        JOptionPane.showMessageDialog(this,
            aboutText,
            "À propos",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void exitApplication() {
        int option = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir quitter l'application ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            // Arrêter le timer de l'horloge
            if (clockTimer != null) {
                clockTimer.stop();
            }

            // Nettoyer les ressources de la base de données
            try {
                DatabaseManager.getInstance().shutdown();
            } catch (Exception e) {
                System.err.println("Erreur lors de la fermeture: " + e.getMessage());
            }

            System.exit(0);
        }
    }

    /**
     * Méthode pour afficher la fenêtre principale
     * Cette méthode est appelée depuis BibliothequeApp
     */
    public void showWindow() {
        setVisible(true);
        // Actualiser le tableau de bord au démarrage
        if (dashboardPanel != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    dashboardPanel.refresh();
                } catch (Exception e) {
                    System.err.println("Erreur lors du rafraîchissement initial du dashboard: " + e.getMessage());
                }
            });
        }
    }

    // Méthodes utilitaires pour accéder aux panneaux
    public void showBooksTab() {
        if (tabbedPane != null && booksPanel != null) {
            tabbedPane.setSelectedComponent(booksPanel);
        }
    }

    public void showMembersTab() {
        if (tabbedPane != null && membersPanel != null) {
            tabbedPane.setSelectedComponent(membersPanel);
        }
    }

    public void showLoansTab() {
        if (tabbedPane != null && loansPanel != null) {
            tabbedPane.setSelectedComponent(loansPanel);
        }
    }

    public void showDashboardTab() {
        if (tabbedPane != null && dashboardPanel != null) {
            tabbedPane.setSelectedComponent(dashboardPanel);
        }
    }

    // Getters pour les panneaux (utile pour les tests)
    public BooksPanel getBooksPanel() {
        return booksPanel;
    }

    public MembersPanel getMembersPanel() {
        return membersPanel;
    }

    public LoansPanel getLoansPanel() {
        return loansPanel;
    }

    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public String getCurrentUser() {
        return currentUser;
    }
}
