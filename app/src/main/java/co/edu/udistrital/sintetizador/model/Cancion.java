package co.edu.udistrital.sintetizador.model;

import java.util.List;

public class Cancion {

    //Este es para el para almacenar el tiempo, y su variable estática se encuentra en TipoTiempo
    private int t;

    private String notas;

    private transient List<NotaSeleccionada> list;

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    public List<NotaSeleccionada> getList() {
        return list;
    }

    public void setList(List<NotaSeleccionada> list) {
        this.list = list;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
