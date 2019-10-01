package co.edu.udistrital.sintetizador.model;

import java.util.List;

public class Cancion {

    //Este es para el para almacenar el tiempo, y su variable estática se encuentra en TipoTiempo
    private int t;

    private int l;

    private List<NotaSeleccionada> list;

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    public int getL() {
        return l;
    }

    public void setL(int l) {
        this.l = l;
    }

    public List<NotaSeleccionada> getList() {
        return list;
    }

    public void setList(List<NotaSeleccionada> list) {
        this.list = list;
    }
}
