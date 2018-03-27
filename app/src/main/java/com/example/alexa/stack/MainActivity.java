package com.example.alexa.stack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends Activity {


    public user[] date;
    public ListView lista;
    public Long data_age;

    private class user
    {
        String nume="";
        Bitmap poza_profil;
        int nr_medlii_bronz,nr_medalii_argint, nr_medalii_aur;
        public user(JSONObject jobj) {
            try {
                this.nume=jobj.getString("display_name");
                String image_url=jobj.getString("profile_image");
                URL url = new URL(image_url);
                this.poza_profil = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                jobj=jobj.getJSONObject("badge_counts");
                this.nr_medlii_bronz=jobj.getInt("bronze");
                this.nr_medalii_argint=jobj.getInt("silver");
                this.nr_medalii_aur=jobj.getInt("gold");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public user()
        {

        }

    }

    private class DataExtractor extends AsyncTask<String,String,user[]>
    {

        String link="https://api.stackexchange.com/2.2/users?order=desc&sort=reputation&site=stackoverflow";


        @Override
        protected user[] doInBackground(String... strings) {

            {
                ///Incearca sa se conecteze
                ///proceseaza jsonul
                user[] users = new user[10];
                try {
                    URL url = new URL(link);
                    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(30000);
                    con.setReadTimeout(30000);
                    con.connect();
                    String a = con.getResponseMessage();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String content = "", line;
                    while ((line = rd.readLine()) != null) {
                        content += line + "\n";
                    }

                    JSONObject item = new JSONObject(content);
                    JSONArray items = item.getJSONArray("items");
                    for (int i = 0; i < 10; i++)
                        users[i] = new user(items.getJSONObject(i));

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                return users;
            }
        }

        @Override
        protected void onPostExecute(user[] result)
        {
                date = result;
                CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), result);
                lista.setAdapter(customAdapter);
                data_age = System.currentTimeMillis() / 1000;
            File myfile=new File(getApplicationContext().getFilesDir(),"/date");
            try {
                FileOutputStream file = new FileOutputStream(myfile);
                BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(file));
                String de_scris="";
                de_scris+=(String.valueOf(data_age)+"\n");
                File save_dir=new File(getApplicationContext().getFilesDir()+"/saved_photos");
                save_dir.mkdirs();

                for (int i = 0; i < result.length; i++) {
                 de_scris+=(result[i].nume+"\n");
                 de_scris+=(String.valueOf(result[i].nr_medlii_bronz+"\n"));
                 de_scris+=(String.valueOf(result[i].nr_medalii_argint+"\n"));
                 de_scris+=(String.valueOf(result[i].nr_medalii_aur+"\n"));
                 String name=String.valueOf(i)+".jpg";
                 File f=new File(save_dir,name);
                 if(f.exists())f.delete();
                 FileOutputStream out = new FileOutputStream(f);
                 result[i].poza_profil.compress(Bitmap.CompressFormat.JPEG,90,out);
                 out.flush();
                 out.close();
                }
                writer.append(de_scris);
                writer.flush();
                writer.close();
                file.close();
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public class CustomAdapter extends BaseAdapter
    {//adapter pentru lista

        Context context;
        String[] nume=new String[10];
        Bitmap[] poza_profil=new Bitmap[10];
        LayoutInflater inf;

        public CustomAdapter(Context appcon,user[] users)
        {
            this.context=appcon;
            inf=LayoutInflater.from(appcon);
            int i;
            for(i=0;i<users.length;i++)
            {
                this.nume[i]=users[i].nume;
                this.poza_profil[i]=users[i].poza_profil;
            }
        }

        @Override
        public int getCount() {
            return nume.length;
        }

        @Override
        public user getItem(int i) {
            return date[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            view=inf.inflate(R.layout.lista,null);
            TextView txt=(TextView) view.findViewById(R.id.nume);
            ImageView im=(ImageView) view.findViewById(R.id.poza_profil);
            txt.setText(nume[i]);
            im.setImageBitmap(poza_profil[i]);
            return view;
        }

    }


    class doer
    {
        public boolean check_internet() //verifica daca exita conexiune
        {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
                return true;
            else return false;
        }

        public void get_data() {


                data_age = System.currentTimeMillis();
                if ((System.currentTimeMillis() / 1000 - data_age <= 600) && date != null) { //verifica daca datele din ram exista sunt curente
                    CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), date);
                    lista.setAdapter(customAdapter);
                }
                else {
                    try {
                        /*
                        verifica daca datele de pe hard exista si sunt curente
                         */
                        File myfile = new File(getApplicationContext().getFilesDir(), "/date");
                        myfile.createNewFile();
                        FileInputStream file = new FileInputStream(myfile);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(file));
                        String line = reader.readLine();
                        if (line == null) {//daca nu exista descarca altele
                            file.close();
                            if(check_internet()==true) {
                                DataExtractor d = new DataExtractor();
                                d.execute();
                            }
                        } else {
                            Long age = Long.parseLong(line);
                            Long cur = System.currentTimeMillis();
                            if (System.currentTimeMillis() / 1000 - age > 30 * 60) {
                                file.close();
                                if(check_internet()) {//daca exista dar sunt expirate cauta altele
                                    DataExtractor d = new DataExtractor();
                                    d.execute();
                                }
                            } else {//salveaza datele pe hard dupa ce a extras unele noi si momentul la care au fost descarcate
                                date = new user[10];
                                String ext = "";
                                for (int i = 0; i <= 9; i++) {
                                    date[i] = new user();
                                    ext = reader.readLine();
                                    date[i].nume = ext;
                                    ext = reader.readLine();
                                    date[i].nr_medlii_bronz = Integer.parseInt(ext);
                                    ext = reader.readLine();
                                    date[i].nr_medalii_argint = Integer.parseInt(ext);
                                    ext = reader.readLine();
                                    date[i].nr_medalii_aur = Integer.parseInt(ext);
                                    String path = getApplicationContext().getFilesDir().getAbsolutePath() + "/saved_photos/" + String.valueOf(i) + ".jpg";
                                    File img = new File(path);
                                    date[i].poza_profil = BitmapFactory.decodeFile(img.getAbsolutePath());
                                }
                                CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), date);
                                lista.setAdapter(customAdapter);
                            }
                        }
                        file.close();
                        reader.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.standard);



        lista=(ListView) findViewById(R.id.lista);
        Button b=findViewById(R.id.button);
        final doer manager=new doer();
        b.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                manager.get_data();
            }
        });
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // daca un item a fost selectat se deschide noul ecran si se afiseaza datele corespunzatoare
                user u=(user) lista.getItemAtPosition(position);
                setContentView(R.layout.user_profile);
                ImageView img=(ImageView) findViewById(R.id.profil_pozadeprofil);
                img.setImageBitmap(u.poza_profil);
                TextView txt =(TextView) findViewById(R.id.profil_argint);
                txt.setText("Medalii de argint: "+String.valueOf(u.nr_medalii_argint));
                txt =(TextView) findViewById(R.id.profil_bronz);
                txt.setText("Medalii de bronz: "+String.valueOf(u.nr_medalii_argint));
                txt =(TextView) findViewById(R.id.profil_aur);
                txt.setText("Medalii de aur: "+String.valueOf(u.nr_medalii_aur));
                txt =(TextView) findViewById(R.id.profil_name);
                txt.setText(u.nume);

                Button back_button=(Button) findViewById(R.id.profil_back);
                back_button.setOnClickListener(new View.OnClickListener() {


                    public void onClick(View v) {
                        //se reincarca activitatea si vechiul layout
                        recreate();
                        setContentView(R.layout.standard);
                    }


                });

            }
        });
        manager.get_data();//populeaza lista la inceput sau daca se reincarca layout-ul iar



    }
}
