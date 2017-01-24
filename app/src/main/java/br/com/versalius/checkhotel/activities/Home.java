package br.com.versalius.checkhotel.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import br.com.versalius.checkhotel.R;
import br.com.versalius.checkhotel.network.NetworkHelper;
import br.com.versalius.checkhotel.network.ResponseCallback;
import br.com.versalius.checkhotel.utils.CustomSnackBar;
import br.com.versalius.checkhotel.utils.ProgressDialogHelper;
import br.com.versalius.checkhotel.utils.SessionHelper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String DOMINIO = "http://checkhotel.versalius.com.br/";
    private ImageButton btCheckin;
    private ImageButton btCheckout;
    private TextView tvName;
    private TextView tvEmail;
    private ImageView ivAvatar;
    private CoordinatorLayout coordinatorLayout;
    SessionHelper sessionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionHelper = new SessionHelper(Home.this);
        if (!sessionHelper.isLogged()) {
            startActivity(new Intent(Home.this, LoginActivity.class));
        } else {
            setContentView(R.layout.activity_home);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

        /* Manipula os itens do Navigation Drawer */
            View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_home);
            tvName = (TextView) headerLayout.findViewById(R.id.tvName);
            tvEmail = (TextView) headerLayout.findViewById(R.id.tvEmail);
            tvName.setText(sessionHelper.getUserName());
            tvEmail.setText(sessionHelper.getUserEmail());
            ivAvatar = (ImageView) headerLayout.findViewById(R.id.ivAvatar);

            if (!sessionHelper.getAvatar().equals("null")) {
                try {
                    new DownloadImageTask(ivAvatar).execute(DOMINIO + sessionHelper.getAvatar());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ivAvatar.setImageResource(R.drawable.toolbar_logo);
            }


        /* Manipula os fragmentos no Navigation Drawer */

            btCheckin = (ImageButton) findViewById(R.id.btCheckin);
            btCheckin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Home.this, CheckInActivity.class));
                }
            });

            btCheckout = (ImageButton) findViewById(R.id.btCheckout);
            btCheckout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Home.this, CheckOutActivity.class));
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }
    */

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_update_profile:
                startActivity(new Intent(Home.this, ProfileAcitvity.class));
                break;

            case R.id.nav_alter_password:
                startActivity(new Intent(Home.this, AlterPasswordActivity.class));
                break;

            case R.id.nav_delete_acc:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                final ProgressDialogHelper progressHelper = new ProgressDialogHelper(Home.this);
                                progressHelper.createProgressSpinner("Aguarde", "Excluindo conta.", true, false);
                                NetworkHelper.getInstance(Home.this).userDelete(sessionHelper.getUserId(), sessionHelper.getUserKey(), new ResponseCallback() {
                                    @Override
                                    public void onSuccess(String jsonStringResponse) {
                                        try {
                                            progressHelper.dismiss();
                                            JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                            if (jsonObject.getBoolean("status")) {
                                                CustomSnackBar.make(coordinatorLayout, "Conta Excluída", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.SUCCESS).show();
                                                sessionHelper.logout();
                                                startActivity(new Intent(Home.this, LoginActivity.class));
                                            } else {
                                                CustomSnackBar.make(coordinatorLayout, "Falha ao excluir conta", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFail(VolleyError error) {
                                        progressHelper.dismiss();
                                        CustomSnackBar.make(coordinatorLayout, "Falha ao realizar cadastro", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                    }
                                });
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //botão NÃO clicado
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Tem certeza que deseja excluir sua conta?").setPositiveButton("SIM", dialogClickListener)
                        .setNegativeButton("NÃO", dialogClickListener).show();
                break;


            case R.id.nav_logout:
                sessionHelper.logout();
                startActivity(new Intent(Home.this, LoginActivity.class));
                break;
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
