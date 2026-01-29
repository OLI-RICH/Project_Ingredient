package model;

public enum PaymentStatus {
    PENDING,     // En attente (valeur par défaut)
    PAID,        // Payée → permet de créer la vente
    CANCELLED,   // Annulée
    REFUNDED     // Remboursée
}