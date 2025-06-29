package com.bibliotheque.ui.panels;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {
    
    private JLabel welcomeLabel;
    private JPanel statsPanel;
    private JPanel quickActionsPanel;

    public DashboardPanel() {
        initializeComponents();
        setupLayout();
        loadDashboardData();
    }

    private void initializeComponents() {
        // Label de bienvenue
        welcomeLabel = new JLabel("Bienvenue dans le Système de Gestion de Bibliothèque");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(70, 130, 180));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Panneau des statistiques
        statsPanel = createStatsPanel();

        // Panneau des actions rapides
        quickActionsPanel = createQuickActionsPanel();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Titre en haut
        add(welcomeLabel, BorderLayout.NORTH);

        // Contenu principal
        JPanel mainContent = new JPanel(new GridLayout(2, 1, 10, 10));
        mainContent.setBackground(Color.WHITE);
        mainContent.add(statsPanel);
        mainContent.add(quickActionsPanel);

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Statistiques"));
        panel.setBackground(Color.WHITE);

        // Cartes de statistiques
        panel.add(createStatCard("Livres", "0", new Color(52, 152, 219)));
        panel.add(createStatCard("Membres", "0", new Color(46, 204, 113)));
        panel.add(createStatCard("Emprunts actifs", "0", new Color(241, 196, 15)));
        panel.add(createStatCard("Retards", "0", new Color(231, 76, 60)));

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createLineBorder(color, 2));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(200, 100));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(color);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setForeground(color);

        card.add(Box.createVerticalGlue());
        card.add(titleLabel);
        card.add(valueLabel);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.setBorder(BorderFactory.createTitledBorder("Actions rapides"));
        panel.setBackground(Color.WHITE);

        // Boutons d'actions rapides
        JButton addBookBtn = createActionButton("Ajouter un livre", new Color(52, 152, 219));
        JButton addMemberBtn = createActionButton("Ajouter un membre", new Color(46, 204, 113));
        JButton newLoanBtn = createActionButton("Nouvel emprunt", new Color(241, 196, 15));
        JButton viewOverdueBtn = createActionButton("Voir les retards", new Color(231, 76, 60));

        panel.add(addBookBtn);
        panel.add(addMemberBtn);
        panel.add(newLoanBtn);
        panel.add(viewOverdueBtn);

        return panel;
    }

    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(150, 40));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);

        // Ajouter des actions aux boutons (à implémenter selon les besoins)
        button.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Fonctionnalité '" + text + "' à implémenter", 
                "Information", 
                JOptionPane.INFORMATION_MESSAGE);
        });

        return button;
    }

    private void loadDashboardData() {
        // Cette méthode peut être appelée pour charger les données du dashboard
        SwingUtilities.invokeLater(() -> {
            try {
                // Ici vous pouvez charger les statistiques réelles
                // updateStats();
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement des données du dashboard: " + e.getMessage());
            }
        });
    }

    public void refresh() {
        loadDashboardData();
        revalidate();
        repaint();
    }

    // Méthode pour mettre à jour les statistiques (à implémenter)
    private void updateStats() {
        // TODO: Implémenter la récupération des statistiques réelles
        // depuis la base de données
    }
}
