package br.com.versalius.checkhotel;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import br.com.versalius.checkhotel.activities.AlterPasswordActivity;
import br.com.versalius.checkhotel.activities.CheckInActivity;
import br.com.versalius.checkhotel.activities.CheckOutActivity;
import br.com.versalius.checkhotel.activities.LoginActivity;
import br.com.versalius.checkhotel.activities.ProfileAcitvity;
import br.com.versalius.checkhotel.network.NetworkHelper;
import br.com.versalius.checkhotel.network.ResponseCallback;
import br.com.versalius.checkhotel.utils.CustomSnackBar;
import br.com.versalius.checkhotel.utils.ProgressBarHelper;
import br.com.versalius.checkhotel.utils.SessionHelper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String DOMINIO = "http://checkhotel.versalius.com.br/";
    private AppCompatButton btCheckin;
    private AppCompatButton btCheckout;
    private TextView tvName;
    private TextView tvEmail;
    private ImageView ivAvatar;
    private CoordinatorLayout coordinatorLayout;
    private final int PROFILE_CODE = 1;
    private final int ALTER_PASSWORD_CODE = 2;
    private final int CHECKIN_CODE = 3;
    private final int CHECKOUT_CODE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionHelper.getInstance(this.getApplicationContext());
        if (!SessionHelper.isLogged()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            setContentView(R.layout.activity_main);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setElevation(0);

            coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

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
            tvName.setText(SessionHelper.getUserName());
            tvEmail.setText(SessionHelper.getUserEmail());
            ivAvatar = (ImageView) headerLayout.findViewById(R.id.ivAvatar);

            if (!SessionHelper.getAvatar().equals("null")) {
                try {
                    new DownloadImageTask(ivAvatar).execute(DOMINIO + SessionHelper.getAvatar());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ivAvatar.setImageResource(R.drawable.toolbar_logo);
            }


        /* Manipula os fragmentos no Navigation Drawer */

            btCheckin = (AppCompatButton) findViewById(R.id.btCheckin);
            btCheckin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivityForResult(new Intent(MainActivity.this, CheckInActivity.class), CHECKIN_CODE);
                }
            });

            btCheckout = (AppCompatButton) findViewById(R.id.btCheckout);
            btCheckout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivityForResult(new Intent(MainActivity.this, CheckOutActivity.class), CHECKOUT_CODE);
                }
            });
        }
    }

//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_update_profile:
                startActivityForResult(new Intent(MainActivity.this, ProfileAcitvity.class), PROFILE_CODE);
                break;

            case R.id.nav_alter_password:
                startActivityForResult(new Intent(MainActivity.this, AlterPasswordActivity.class), ALTER_PASSWORD_CODE);
                break;

            case R.id.nav_delete_acc:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                final ProgressBarHelper progressHelper = new ProgressBarHelper(MainActivity.this,null);
                                progressHelper.createProgressSpinner();
                                NetworkHelper.getInstance(MainActivity.this).userDelete(new ResponseCallback() {
                                    @Override
                                    public void onSuccess(String jsonStringResponse) {
                                        try {
                                            progressHelper.dismiss();
                                            JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                            if (jsonObject.getBoolean("status")) {

                                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                builder.setMessage("Conta excluída").setCancelable(false).setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        startActivity(new Intent(MainActivity.this,LoginActivity.class));
                                                        SessionHelper.logout();
                                                        finish();
                                                    }
                                                }).show();

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
                                        CustomSnackBar.make(coordinatorLayout, "Falha ao excluir conta", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
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
                SessionHelper.logout();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PROFILE_CODE:
                if (resultCode == ProfileAcitvity.RESULT_OK)
                    CustomSnackBar.make(coordinatorLayout, "Atualização realizada com sucesso", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.SUCCESS).show();
                break;
            case ALTER_PASSWORD_CODE:
                if (resultCode == AlterPasswordActivity.RESULT_OK)
                    CustomSnackBar.make(coordinatorLayout, "Senha alterada", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.SUCCESS).show();
                break;
            case CHECKIN_CODE:
                if (resultCode == CheckInActivity.RESULT_OK)
                    CustomSnackBar.make(coordinatorLayout, "Check-in realizado com sucesso", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.SUCCESS).show();
                break;
            case CHECKOUT_CODE:
                if (resultCode == CheckInActivity.RESULT_OK)
                    CustomSnackBar.make(coordinatorLayout, "Check-out realizado com sucesso", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.SUCCESS).show();
                break;
        }
    }
}
