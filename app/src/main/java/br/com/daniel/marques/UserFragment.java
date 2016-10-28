package br.com.daniel.marques;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UserFragment extends Fragment implements  View.OnClickListener {

    /*
        Novas informações de usuarioa podem ser acrescentadas adicionando mais uma chave em "KEYS_VALUE" , mais
     um id do EditText correspondente, e um valor padrão em "values".
    */
    private static final String[] KEYS_VALUE ={"name","trabalho"};
    private static final int[] EDITS_ID ={R.id.nome,R.id.trabalho};
    private  String[] values ={"",""};

    private static final String VALUES = "values";

    public static String USERS ="users";
    public static String INFO_USER ="info_user";



    private OnFragmentInteractionListener mListener;

    public UserFragment() {
        // Required empty public constructor
    }



    public static UserFragment newInstance(String param1, String param2) {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    DatabaseReference reference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.fragment_user, container, false);
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser(); //obtem o usuario logado.

        //obtêm a referencia do banco de ados para informações de um determinado usuario.
        reference=FirebaseDatabase.getInstance().getReference().child(USERS).child(user.getUid()).child(INFO_USER);

        if(savedInstanceState==null){
            //Lê no banco de dados as informações para usuario corrente e preenche os editText do layout.
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (int i=0;i<KEYS_VALUE.length;i++){ //percorre todas as chaves definidas
                       EditText editText= (EditText) view.findViewById(EDITS_ID[i]);
                        if(dataSnapshot.hasChild(KEYS_VALUE[i])){ // verifica se essa chave foi gravada no banco de dados
                            editText.setText((String) dataSnapshot.child(KEYS_VALUE[i]).getValue());
                            values[i]=(String) dataSnapshot.child(KEYS_VALUE[i]).getValue();
                        }else {
                            editText.setText(values[i]);
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        view.findViewById(R.id.button_save).setOnClickListener(this);
        return view;
    }







    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onClick(final View v) {

        //Quando o botão se salvar é clicado
        if(v.getId()==R.id.button_save){
            v.setEnabled(false);
            Map<String,Object> map= new HashMap<>(); // Map com os valores que serão gravados no Banco

            for(int i=0;i<KEYS_VALUE.length;i++){
                EditText editText= (EditText) getView().findViewById(EDITS_ID[i]);
                if(!values[i].equals(editText.getText().toString())){// verifica e a informação foi modificada
                    values[i]=editText.getText().toString();
                    map.put(KEYS_VALUE[i],editText.getText().toString()); //adiciona um determinado valor no Banco
                }
            }
            if(map.size()>0){// grava se houver pelo menos um item
                reference.updateChildren(map).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getContext(),getContext().getString(R.string.salvo_sucesso),Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(getContext(),getContext().getString(R.string.erro_salvar),Toast.LENGTH_LONG).show();
                        }
                        v.setEnabled(true);

                    }
                });
            }else{
                v.setEnabled(true);
            }



        }


    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
