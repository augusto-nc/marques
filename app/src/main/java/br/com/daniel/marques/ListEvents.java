package br.com.daniel.marques;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Jose_Augusto on 25/10/2016.
 */
public class ListEvents extends ListFragment implements AdapterView.OnItemClickListener {


    DatabaseReference reference;
    ArrayList<Evento> eventos;
    EventosAdapter adapter;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference().child(UserFragment.USERS).child(firebaseUser.getUid()).child(NewEventFragment.LIST_EVTS);
        adapter= new EventosAdapter();
        adapter= new EventosAdapter();
        eventos= new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View v= super.onCreateView(inflater, container, savedInstanceState);


        reference.addListenerForSingleValueEvent(new ValueEventListener() {

            //ler a lista de eventos do usuario
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> childs =dataSnapshot.getChildren();
                eventos= new ArrayList<Evento>(); // cria uma nova lista de eventos

                for(DataSnapshot snap:childs){
                    Evento evento= new Evento();
                    evento.nomeEvento = (String) snap.child(Evento.NOME_EVENTO).getValue();
                    evento.tipoEvento = (int) (long) snap.child(Evento.TIPO_EVENTO).getValue();
                    evento.idEvento = snap.getKey();
                    eventos.add(evento); //adiciona o evento na lista de eventos
                }
                adapter.notifyDataSetChanged(); // notifica o adapter para atualizar a lista.
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        EventoSelected eventoSelected= (EventoSelected) getActivity();
       eventoSelected.onEventoSelected(((Evento)adapter.getItem(position)).idEvento);//chama o metodo na MainActivity para abrir o fragment dos eventos.
    }


    private class EventosAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return eventos.size();
        }

        @Override
        public Object getItem(int position) {
            return eventos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater= (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view= layoutInflater.inflate(R.layout.view_evt,null);
            ((TextView)view.findViewById(R.id.txt_nome_evt)).setText(eventos.get(position).nomeEvento);
            ((TextView)view.findViewById(R.id.txt_tipo_evt)).setText(NewEventFragment.TIPOS_EVENTOS[eventos.get(position).tipoEvento]);
            return view;
        }
    }


   public interface EventoSelected {
        void onEventoSelected(String id);
    }

}
