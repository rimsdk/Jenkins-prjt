package com.ehei;

public class App {
    public static void main(String[] args) {
        BankAccount account = new BankAccount("John Doe", 1000.0);
        System.out.println("Bienvenue dans l'application bancaire !");
        System.out.println("Titulaire : " + account.getAccountHolder());
        System.out.println("Solde initial : " + account.getBalance());
    }
}
