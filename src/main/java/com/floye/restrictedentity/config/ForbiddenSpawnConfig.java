package com.floye.restrictedentity.config;

import java.util.ArrayList;
import java.util.List;

public class ForbiddenSpawnConfig {

    // Liste de zones restreintes
    public List<RestrictedZone> restrictedZones = new ArrayList<>();

    // Zone restreinte (chaque zone porte un nom pour pouvoir la supprimer ensuite)
    public static class RestrictedZone {
        public String name;
        public String entity;
        public String dimension;
        public Position min;
        public Position max;
        public String spawnType; // "all" ou "natural"

        // Constructeur
        public RestrictedZone(String name, String entity, String dimension, Position min, Position max, String spawnType) {
            this.name = name;
            this.entity = entity;
            this.dimension = dimension;
            this.min = min;
            this.max = max;
            this.spawnType = spawnType;
        }
    }

    // Position (min ou max)
    public static class Position {
        public int x;
        public int y;
        public int z;

        public Position(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}