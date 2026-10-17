package com.example.mygamingapp;


import java.util.Locale;

public class ItemModel {
    private long id;
    private String horaInicio;
    private String minutoInicio;
    private String horaFim;
    private String minutoFim;

    public ItemModel(long id, String horaInicio, String minutoInicio, String horaFim, String minutoFim) {
        this.id = id;
        this.horaInicio = horaInicio;
        this.minutoInicio = minutoInicio;
        this.horaFim = horaFim;
        this.minutoFim = minutoFim;
    }

    // MÉTODO NOVO: Calcula a duração desta linha específica
    public String obterDuracaoSessao() {
        try {
            int hIni = Integer.parseInt(horaInicio);
            int mIni = Integer.parseInt(minutoInicio);
            int hFim = Integer.parseInt(horaFim);
            int mFim = Integer.parseInt(minutoFim);

            int minutosInicio = (hIni * 60) + mIni;
            int minutosFim = (hFim * 60) + mFim;
            int diferenca;

            if (minutosFim >= minutosInicio) {
                diferenca = minutosFim - minutosInicio;
            } else {
                diferenca = ((1440 - minutosInicio) + minutosFim); // Passou da meia-noite
            }

            int hFinal = diferenca / 60;
            int mFinal = diferenca % 60;

            if (hFinal == 0 && mFinal == 0) return ""; // Se estiver zerado (00:00 às 00:00), não mostra nada
            return String.format(Locale.getDefault(), " (+%02d:%02d)", hFinal, mFinal);
        } catch (NumberFormatException e) {
            return "";
        }
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
    public String getMinutoInicio() { return minutoInicio; }
    public void setMinutoInicio(String minutoInicio) { this.minutoInicio = minutoInicio; }
    public String getHoraFim() { return horaFim; }
    public void setHoraFim(String horaFim) { this.horaFim = horaFim; }
    public String getMinutoFim() { return minutoFim; }
    public void setMinutoFim(String minutoFim) { this.minutoFim = minutoFim; }
}
