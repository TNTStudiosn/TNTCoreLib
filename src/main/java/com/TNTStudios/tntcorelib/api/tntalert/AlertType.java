// Ubicación: src/main/java/com/TNTStudios/tntcorelib/api/tntalert/AlertType.java
package com.TNTStudios.tntcorelib.api.tntalert;

/**
 * Mi enumeración para los diferentes estilos de alerta que puedo mostrar.
 * Esto me permite estandarizar los diseños y simplificar las llamadas a la API.
 */
public enum AlertType {
    NOTIFICATION, // Para notificaciones generales, como un toast.
    WARNING,      // Para avisos importantes que requieren atención.
    URGENT,       // Para alertas críticas que no deben ser ignoradas.
    TIP;          // Para consejos o información útil no intrusiva.
}