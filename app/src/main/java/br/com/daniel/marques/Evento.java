package br.com.daniel.marques;

import java.io.Serializable;

/**
 * Created by Jose_Augusto on 23/10/2016.
 */
public class Evento implements Serializable {
    public String nomeEvento;
    public int tipoEvento;
    public String idEvento;
    public String link;
    public  static final String NOME_EVENTO="nome";
    public  static final String TIPO_EVENTO="tipo";
    public  static final String LINK="link";


    public Evento() {
       nomeEvento="";
       tipoEvento=-1;
       idEvento="";
        link="";
    }
}
