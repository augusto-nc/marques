package br.com.daniel.marques;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;


public class NewEventFragment extends Fragment implements View.OnClickListener {
    private static final String EVENTO = "evt";
    private static final String ID = "id";
    private static final String ARG_PARAM2 = "param2";
    public static final String LIST_EVTS = "list_evts";
    private static final String LINK= "link";
    public static final String[] TIPOS_EVENTOS = {"SOCIAL","BALADA","INFANTIL","FLASH-BACK"};
    private static final int REQUEST_IMAGE_CAPTURE = 15;
    private static final String CURRENT_PHOTO = "curren";
    private static final int SELECT_PHOTO = 14;


    // TODO: Rename and change types of parameters
    private Evento evento;

    DatabaseReference reference;
    private OnFragmentInteractionListener mListener;

    public NewEventFragment(){


    }

//Recebe o id do evento que será aberto, se id for null, então é para criar um novo evento.
    public static NewEventFragment newInstance(String id) {
        NewEventFragment fragment = new NewEventFragment();
        Bundle args = new Bundle();
        args.putString(ID, id);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference().child(UserFragment.USERS).child(firebaseUser.getUid()).child(LIST_EVTS);
    }

    EditText editNomeEvento;
    ListView listTipoEvento;
    TipoAdapter tipoAdapter;
    Button buttonAplicar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v=inflater.inflate(R.layout.fragment_novo_evento, container, false);

        listTipoEvento= (ListView) v.findViewById(R.id.list_tipo_evento); //lista para selecionar o tipo de evento
        editNomeEvento= (EditText) v.findViewById(R.id.nome); // editText do nome do evento
        tipoAdapter=new TipoAdapter();
        listTipoEvento.setAdapter(tipoAdapter);
        buttonAplicar= (Button) v.findViewById(R.id.button_save); // Botão salvar/ criar novo

        listTipoEvento.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                evento.tipoEvento=position;
                tipoAdapter.notifyDataSetChanged();
            }
        });


        if (savedInstanceState==null) {
             evento= new Evento();
            final String idEvento = getArguments().getString(ID);
            if(idEvento!=null){// Se o id nao for null ler o o evento
                reference.child(idEvento).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        evento.nomeEvento= (String) dataSnapshot.child(Evento.NOME_EVENTO).getValue();
                        evento.tipoEvento= (int)(long) dataSnapshot.child(Evento.TIPO_EVENTO).getValue();
                        evento.link= (String) dataSnapshot.child(Evento.LINK).getValue();
                        Picasso.with(getActivity()).load(evento.link.equals("") ? "adr" : evento.link).placeholder(android.R.drawable.ic_menu_camera)
                                .error(android.R.drawable.ic_menu_camera).into((ImageView) getView().findViewById(R.id.foto));
                        evento.idEvento=idEvento;
                        tipoAdapter.notifyDataSetChanged();
                        buttonAplicar.setText(getContext().getString(R.string.salvar));
                        editNomeEvento.setText(evento.nomeEvento);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }else{//coloca valores padrões no campos do evento.
                evento.nomeEvento= "";
                evento.tipoEvento= -1;
                evento.idEvento=null;
            }
        }else{
            evento= (Evento) savedInstanceState.getSerializable(EVENTO);
        }


        if(evento.idEvento==null || evento.idEvento.equals("")){ // se não tiver id, então é para "criar novo"
            buttonAplicar.setText(getContext().getString(R.string.criar_novo));
        }else{
            buttonAplicar.setText(getContext().getString(R.string.salvar));
        }
        buttonAplicar.setOnClickListener(this);
        ImageView foto= (ImageView) v.findViewById(R.id.foto);
        foto.setOnClickListener(this);

        return v;
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EVENTO,evento);
        outState.putSerializable(CURRENT_PHOTO,mCurrentPhotoPath);
    }

    @Override
    public void onClick(final View v) {
        if(v.getId()==R.id.button_save) {// se tive clicado no botão
            String nome=editNomeEvento.getText().toString();

            if(nome.length()<5){//Nome do evento deve ter pelo menos 5 caracteres
                Toast.makeText(getContext(),R.string.nome_curto,Toast.LENGTH_LONG).show();
                return;
            }
            if(evento.tipoEvento==-1){ // tipo do evento deve ter sido selecionado
                Toast.makeText(getContext(),R.string.escolha_um_tipo,Toast.LENGTH_LONG).show();
                return;
            }
            v.setEnabled(false);

            if(evento.idEvento==null){ //Se o id do evento for null, então cria um novo envento no banco de dados
                DatabaseReference ref= reference.push();
                evento.idEvento=ref.getKey();
            }
            Map<String,Object> map= new HashMap<>();
            evento.nomeEvento=nome;
            map.put(Evento.NOME_EVENTO,nome);
            map.put(Evento.TIPO_EVENTO,evento.tipoEvento);
            map.put(Evento.LINK,evento.link);
            //Grava no banco de dados a informações do evento
            reference.child(evento.idEvento).updateChildren(map).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getContext(),R.string.salvo_sucesso,Toast.LENGTH_LONG).show();
                        ((Button)v).setText(R.string.salvar);
                    }else {
                        Toast.makeText(getContext(),R.string.erro_salvar,Toast.LENGTH_LONG).show();
                    }
                    v.setEnabled(true);

                }
            });

        }else if(v.getId()==R.id.foto){// se tiver clicado na foto
            registerForContextMenu(v);
            getActivity().openContextMenu(v);// abre o contextMenu
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            tirarFoto();
        } else {//selecionar foto da galeria
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);
        }
        return super.onContextItemSelected(item);

    }

    String mCurrentPhotoPath;


    //cria um endereco de imagem baseado no horario
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp;

        File folder= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        File image = new File(folder, imageFileName + ".jpg");
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        //System.out.println(mCurrentPhotoPath);
        return image;
    }

    //Função para abrir a câmera e tirar uma nova foto
    private void tirarFoto() {
        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //Verifica se existe algum aplicativo disponivel para realizar esta função
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();

                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                    Toast.makeText(getActivity(), getString(R.string.camera_indisponivel), Toast.LENGTH_LONG).show();
                }
                if (photoFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.camera_indisponivel), Toast.LENGTH_LONG).show();
            }
        }


    }




    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    public final static int WIDTH_FOTO =805;
    public final static int HEIGHT_FOTO =453;


    //Reduz o tamanho da imagem
    public static void processarImagem(String fileIn){
        if(fileIn.matches(".*\\.jpg")) {
            File file = new File(fileIn);
            Bitmap b = BitmapFactory.decodeFile(fileIn);
            int w = b.getWidth();
            int h = b.getHeight();
            float fator;
            if (w > h) {
                fator = Math.max(WIDTH_FOTO / (float) w, HEIGHT_FOTO / (float) h);
            } else {
                fator = Math.max(HEIGHT_FOTO / (float) w, WIDTH_FOTO / (float) h);
            }
            w = (int) (w * fator);
            h = (int) (h * fator);
            Bitmap out = Bitmap.createScaledBitmap(b, w, h, false);
            FileOutputStream fOut;
            try {
                fOut = new FileOutputStream(file);
                out.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
                fOut.close();
                b.recycle();
                out.recycle();
            } catch (Exception e) {
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_IMAGE_CAPTURE  || requestCode== SELECT_PHOTO ) && resultCode == Activity.RESULT_OK) {
           if(requestCode==SELECT_PHOTO){
               Uri pickedImage = data.getData();
               // Let's read picked image path using content resolver
               String[] filePath = {MediaStore.Images.Media.DATA};
               Cursor cursor = getActivity().getContentResolver().query(pickedImage, filePath, null, null, null);
               cursor.moveToFirst();
               mCurrentPhotoPath = cursor.getString(cursor.getColumnIndex(filePath[0]));
           }


            processarImagem(mCurrentPhotoPath);
            // galleryAddPic();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference ref = storage.getReference();
            Uri uri = Uri.fromFile(new File(mCurrentPhotoPath));
            UploadTask task = ref.child(uri.getLastPathSegment()).putFile(Uri.fromFile(new File(mCurrentPhotoPath)));

            task.addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.foto_enviada), Toast.LENGTH_LONG);
                    evento.link = taskSnapshot.getDownloadUrl().toString();
                    Picasso.with(getActivity()).load(new File(mCurrentPhotoPath)).placeholder(android.R.drawable.ic_menu_camera).error(android.R.drawable.ic_menu_camera).into((ImageView) getView().findViewById(R.id.foto));
                    onClick(getView().findViewById(R.id.button_save));
                }
            });
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(1, 1, 1, getString(R.string.tirar_foto));
        menu.add(1, 2, 1, getString(R.string.selecionar_galeria));
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    private class TipoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return TIPOS_EVENTOS.length;
        }

        @Override
        public Object getItem(int position) {
            return TIPOS_EVENTOS[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View v = inflater.inflate(android.R.layout.simple_list_item_1, null);
            if(evento.tipoEvento==position){
                v.setBackgroundColor(Color.GREEN);
            }
            TextView textView = (TextView) v.findViewById(android.R.id.text1);
            textView.setText(TIPOS_EVENTOS[position]);
            return v;
        }

    }
}
