package com.floye.restrictedentity.command;

import com.floye.restrictedentity.config.ConfigLoader;
import com.floye.restrictedentity.config.ForbiddenSpawnConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;

public class RestrictedZoneCommand {
    // Instance Gson pour la sérialisation de la config
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("RestrictedZone")
                // Sous-commande pour ajouter une zone
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .then(CommandManager.argument("entity", StringArgumentType.string())
                                        // Suggestions pour l'argument "entity" : liste les identifiants de toutes les entités connues, avec des guillemets.
                                        .suggests((context, builder) -> {
                                            Registries.ENTITY_TYPE.forEach((entityType) -> {
                                                Identifier id = Registries.ENTITY_TYPE.getId(entityType);
                                                if (id != null) {
                                                    // Suggère l'identifiant entouré de guillemets
                                                    builder.suggest("\"" + id.toString() + "\"");
                                                }
                                            });
                                            return builder.buildFuture();
                                        })
                                        .then(CommandManager.argument("dimension", StringArgumentType.string())
                                                // Suggestions pour l'argument "dimension"
                                                .suggests((context, builder) -> {
                                                    builder.suggest("\"minecraft:overworld\"");
                                                    builder.suggest("\"minecraft:the_nether\"");
                                                    builder.suggest("\"minecraft:the_end\"");
                                                    return builder.buildFuture();
                                                })
                                                .then(CommandManager.argument("min_x", IntegerArgumentType.integer())
                                                        .then(CommandManager.argument("min_y", IntegerArgumentType.integer())
                                                                .then(CommandManager.argument("min_z", IntegerArgumentType.integer())
                                                                        .then(CommandManager.argument("max_x", IntegerArgumentType.integer())
                                                                                .then(CommandManager.argument("max_y", IntegerArgumentType.integer())
                                                                                        .then(CommandManager.argument("max_z", IntegerArgumentType.integer())
                                                                                                .executes(context -> {
                                                                                                    String name = StringArgumentType.getString(context, "name");
                                                                                                    // Récupération et suppression des éventuels guillemets
                                                                                                    String entity = StringArgumentType.getString(context, "entity").replaceAll("^\"|\"$", "");
                                                                                                    String dimension = StringArgumentType.getString(context, "dimension").replaceAll("^\"|\"$", "");

                                                                                                    int minX = IntegerArgumentType.getInteger(context, "min_x");
                                                                                                    int minY = IntegerArgumentType.getInteger(context, "min_y");
                                                                                                    int minZ = IntegerArgumentType.getInteger(context, "min_z");
                                                                                                    int maxX = IntegerArgumentType.getInteger(context, "max_x");
                                                                                                    int maxY = IntegerArgumentType.getInteger(context, "max_y");
                                                                                                    int maxZ = IntegerArgumentType.getInteger(context, "max_z");

                                                                                                    // Récupération de la config (la config doit avoir été chargée via ConfigLoader.loadConfig())
                                                                                                    ForbiddenSpawnConfig config = ConfigLoader.getConfig();
                                                                                                    if (config == null) {
                                                                                                        config = new ForbiddenSpawnConfig();
                                                                                                        config.restrictedZones = new java.util.ArrayList<>();
                                                                                                    }
                                                                                                    // Création de la zone à ajouter en utilisant les types internes correctement
                                                                                                    ForbiddenSpawnConfig.Position min = new ForbiddenSpawnConfig.Position(minX, minY, minZ);
                                                                                                    ForbiddenSpawnConfig.Position max = new ForbiddenSpawnConfig.Position(maxX, maxY, maxZ);
                                                                                                    ForbiddenSpawnConfig.RestrictedZone zone =
                                                                                                            new ForbiddenSpawnConfig.RestrictedZone(name, entity, dimension, min, max);

                                                                                                    config.restrictedZones.add(zone);
                                                                                                    ConfigLoader.saveConfig();
                                                                                                    context.getSource().sendFeedback(() -> Text.literal("Zone ajoutée : " + name), true);
                                                                                                    return 1;
                                                                                                })))))))))))
                // Sous-commande pour supprimer une zone par son nom
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    ForbiddenSpawnConfig config = ConfigLoader.getConfig();
                                    if (config == null || config.restrictedZones.isEmpty()) {
                                        context.getSource().sendFeedback(() -> Text.literal("Aucune zone définie."), false);
                                        return 0;
                                    }
                                    // Rechercher et supprimer la zone ayant le nom donné
                                    boolean removed = config.restrictedZones.removeIf(zone -> zone.name.equals(name));
                                    if (removed) {
                                        ConfigLoader.saveConfig();
                                        context.getSource().sendFeedback(() -> Text.literal("Zone supprimée : " + name), true);
                                        return 1;
                                    } else {
                                        context.getSource().sendFeedback(() -> Text.literal("Zone non trouvée : " + name), false);
                                        return 0;
                                    }
                                }))));
    }
}