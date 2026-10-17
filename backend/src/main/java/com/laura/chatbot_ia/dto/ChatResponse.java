
// DTO de salida: define la respuesta que devuelve el chatbot al usuario.
package com.laura.chatbot_ia.dto;

public class ChatResponse {

    // Texto de respuesta generado por el chatbot.
    private String answer;

    // Constructor vacío para facilitar la creación del DTO.
    public ChatResponse() {
    }

    // Constructor para crear una respuesta directamente con su texto.
    public ChatResponse(String answer) {
        this.answer = answer;
    }

    // Devuelve el texto de la respuesta.
    public String getAnswer() {
        return answer;
    }

    // Permite asignar el texto de la respuesta.
    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
