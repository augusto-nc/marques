package br.com.daniel.marques;

import android.graphics.Color;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,ListEvents.EventoSelected {
    private static final String SELECT_FRAME = "select";
    int selectedFrame=R.id.frame_user;// id de um frame layout que envolve os botões do menu
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState==null){
            FragmentTransaction tr= getSupportFragmentManager().beginTransaction();
            tr.replace(R.id.holder,new UserFragment());
            tr.commit();
        }else{
            selectedFrame=savedInstanceState.getInt(SELECT_FRAME);
        }
        findViewById(R.id.bt_novo_evento).setOnClickListener(this);
        findViewById(R.id.bt_usuario).setOnClickListener(this);
        findViewById(R.id.bt_list_eventos).setOnClickListener(this);
        findViewById(selectedFrame).setBackgroundColor(Color.GREEN);

    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECT_FRAME,selectedFrame);
    }

    @Override
    public void onClick(View v) {
        findViewById(selectedFrame).setBackgroundColor(Color.TRANSPARENT);//deixa o frame anterior transparente
        FragmentTransaction tr= getSupportFragmentManager().beginTransaction();
        switch (v.getId()){//Troca o fragment para o novo fragment selecionado
            case R.id.bt_novo_evento:
                selectedFrame=R.id.frame_novo_evento;
                tr.replace(R.id.holder, NewEventFragment.newInstance(null));
                break;
            case R.id.bt_usuario:
                selectedFrame=R.id.frame_user;
                tr.replace(R.id.holder, new UserFragment());
                break;
            case R.id.bt_list_eventos:
                selectedFrame=R.id.frame_list_eventos;
                tr.replace(R.id.holder, new ListEvents());
                break;
        }
        findViewById(selectedFrame).setBackgroundColor(Color.GREEN); // pinta de verde o frame selecionado

        tr.commit();
    }


    @Override
    public void onEventoSelected(String id) {// Quando um evento é selecionado na lista de de eventos

        findViewById(selectedFrame).setBackgroundColor(Color.TRANSPARENT);
        FragmentTransaction tr= getSupportFragmentManager().beginTransaction();
        selectedFrame=R.id.frame_novo_evento; //seleciona o botão de novos eventos
        tr.replace(R.id.holder, NewEventFragment.newInstance(id)); //abre o fragment "NewEventFragment" com o id selecionado
        tr.commit();
        findViewById(selectedFrame).setBackgroundColor(Color.GREEN);//
    }
}
