package com.bibliotheque.utils;

import com.bibliotheque.dao.DatabaseManager;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseRepair {
    
    public static void main(String[] args) {
        System.out.println("=== Réparation rapide de la base de données ===");
        repairDatabase();
    }
    
    public static void repairDatabase() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            Statement stmt = conn.createStatement();
            
            System.out.println("1. Ajout de la colonne location...");
            try {
                stmt.executeUpdate("ALTER TABLE books ADD COLUMN location TEXT");
                System.out.println("   ✅ Colonne ajoutée");
            } catch (Exception e) {
                System.out.println("   ℹ️  Colonne existe déjà ou erreur: " + e.getMessage());
            }
            
            System.out.println("2. Mise à jour des valeurs nulles...");
            int updated = stmt.executeUpdate("UPDATE books SET location = 'Non spécifié' WHERE location IS NULL OR location = ''");
            System.out.println("   ✅ " + updated + " lignes mises à jour");
            
            System.out.println("3. Test de la structure...");
            stmt.executeQuery("SELECT id, title, author, location FROM books LIMIT 1");
            System.out.println("   ✅ Structure validée");
            
            stmt.close();
            System.out.println("\n🎉 Réparation terminée avec succès!");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la réparation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
