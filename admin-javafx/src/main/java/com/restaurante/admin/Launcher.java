package com.restaurante.admin;

// Esta clase existe por una sola razon tecnica: Java no permite ejecutar
// directamente un .jar cuya clase principal extiende de Application (JavaFX).
// Al usar esta clase intermedia como punto de entrada (que NO extiende
// Application), Java la deja pasar sin problema, y ella simplemente
// arranca la app real.
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
