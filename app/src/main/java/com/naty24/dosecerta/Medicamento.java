package com.naty24.dosecerta;

public class Medicamento {
    private long id;                // ID do medicamento
    private String nome;            // Nome do medicamento
    private String ultimaHora;      // Hora da última dose
    private String dataUltima;      // Data da última dose
    private String proximoAlarme;   // Hora do próximo alarme
    private int diasTratamento;     // Dias de tratamento restantes
    private long usuarioId;          // ID do usuário que está usando o medicamento
    private int intervalo;           // Intervalo entre as doses em horas
    private boolean alarmeAtivo;    // Indica se o alarme está ativo

    // Construtor da classe
    public Medicamento(long id, String nome, String ultimaHora, String dataUltima, String proximoAlarme, int diasTratamento, long usuarioId, int intervalo, boolean alarmeAtivo) {
        this.id = id;
        this.nome = nome;
        this.ultimaHora = ultimaHora;        // Inicializa a última hora
        this.dataUltima = dataUltima;        // Inicializa a data da última dose
        this.proximoAlarme = proximoAlarme;  // Inicializa o próximo alarme
        this.diasTratamento = diasTratamento; // Inicializa os dias de tratamento
        this.usuarioId = usuarioId;          // Inicializa o ID do usuário
        this.intervalo = intervalo;          // Inicializa o intervalo
        this.alarmeAtivo = alarmeAtivo;      // Inicializa se o alarme está ativo
    }

    // Métodos getters
    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getUltimaHora() {
        return ultimaHora;
    }

    public String getDataUltima() {
        return dataUltima;
    }

    public String getProximoAlarme() {
        return proximoAlarme;
    }

    public int getDiasTratamento() {
        return diasTratamento;
    }

    public long getUsuarioId() {
        return usuarioId;
    }

    public int getIntervalo() {
        return intervalo;
    }

    public boolean isAlarmeAtivo() {
        return alarmeAtivo;
    }
}