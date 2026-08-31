package br.com.fiap.agrovision.dto.response;

public record TokenResponse(String token, String tipo, Long cpf, String nome, String nomeFazenda) {
}
