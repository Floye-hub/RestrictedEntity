package com.floye.restrictedentity.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.*;

public class ConfigLoader {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    // Chemin vers le fichier de configuration (par exemple dans le dossier config à la racine)
    private static final String CONFIG_PATH = "config/forbidden_spawns.json";

    private static ForbiddenSpawnConfig config;

    public static void loadConfig() {
        File configFile = new File(CONFIG_PATH);

        // Si le fichier de config n'existe pas, on le crée avec une config par défaut (liste vide)
        if (!configFile.exists()) {
            try {
                // Création du dossier parent s'il n'existe pas
                File parentDir = configFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }

                String defaultConfigJson = "{\n  \"restrictedZones\": []\n}";
                try (Writer writer = new FileWriter(configFile)) {
                    writer.write(defaultConfigJson);
                }
                System.out.println("Fichier de configuration par défaut généré à : " + configFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Erreur lors de la génération du fichier de configuration par défaut.");
                e.printStackTrace();
            }
        }

        // Chargement du fichier de configuration
        try (Reader reader = new FileReader(CONFIG_PATH)) {
            config = gson.fromJson(reader, ForbiddenSpawnConfig.class);
            System.out.println("Configuration chargée avec succès !");
        } catch (FileNotFoundException e) {
            System.err.println("Fichier de configuration non trouvé : " + CONFIG_PATH);
        } catch (JsonSyntaxException e) {
            System.err.println("Erreur de syntaxe JSON dans la configuration : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ForbiddenSpawnConfig getConfig() {
        return config;
    }

    // Méthode pour sauvegarder la configuration (après modification par exemple)
    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_PATH)) {
            writer.write(gson.toJson(config));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}