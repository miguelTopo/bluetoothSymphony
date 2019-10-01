package co.edu.udistrital.sintetizador.model;

public class NotaSeleccionada {

    //Este es para almacenar la nota que se encuentra en Nota
    private char n;
    // Este es para almacenar el tipo de nota que se encuentra en TipoNota
    private char nt;

    public NotaSeleccionada(char n, char nt) {
        this.n = n;
        this.nt = nt;
    }

    public char getN() {
        return n;
    }

    public void setN(char n) {
        this.n = n;
    }

    public char getNt() {
        return nt;
    }

    public void setNt(char nt) {
        this.nt = nt;
    }
}
