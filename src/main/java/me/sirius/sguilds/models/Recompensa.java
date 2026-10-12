package me.sirius.sguilds.models;

import java.util.List;

public record Recompensa(String id, List<Opcion> opciones) {

    public record Opcion(String mensaje, List<String> comandos) {}
}
