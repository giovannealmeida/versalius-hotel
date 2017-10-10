package br.com.versalius.checkhotel.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import br.com.versalius.checkhotel.MainActivity;
import br.com.versalius.checkhotel.R;
import br.com.versalius.checkhotel.model.User;
import br.com.versalius.checkhotel.network.NetworkHelper;
import br.com.versalius.checkhotel.network.ResponseCallback;
import br.com.versalius.checkhotel.utils.CustomSnackBar;
import br.com.versalius.checkhotel.utils.ProgressBarHelper;
import br.com.versalius.checkhotel.utils.SessionHelper;

public class LoginActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private EditText etEmail;
    private EditText etPassword;
    private CoordinatorLayout coordinatorLayout;
    private HashMap<String, String> formData;
    private final int SIGNUP_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        formData = new HashMap<>();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setUpViews();
    }

    private void setUpViews() {

        AppCompatButton singup = (AppCompatButton) findViewById(R.id.btSingup);
        TextView forgot = (TextView) findViewById(R.id.btForgot);
        AppCompatButton login = (AppCompatButton) findViewById(R.id.btLogin);

        /* Instanciando campos */
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);

        /* Adicionando FocusListener*/
        etEmail.setOnFocusChangeListener(this);
        etPassword.setOnFocusChangeListener(this);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressBarHelper progressHelper = new ProgressBarHelper(LoginActivity.this, LoginActivity.this.findViewById(R.id.form));
                if (NetworkHelper.isOnline(LoginActivity.this)) {
                    if (isValidForm()) {
                        progressHelper.createProgressSpinner();

                        NetworkHelper.getInstance(LoginActivity.this).login(formData, new ResponseCallback() {
                            @Override
                            public void onSuccess(String jsonStringResponse) {
                                try {
                                    progressHelper.dismiss();
                                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                    if (jsonObject.getBoolean("status")) {
                                        User user = new User(jsonObject.getJSONObject("data"));
                                        SessionHelper.getInstance(LoginActivity.this.getApplicationContext()).saveUser(user);
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        CustomSnackBar.make(coordinatorLayout, "E-mail e/ou senha incorreto(s)", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFail(VolleyError error) {
                                progressHelper.dismiss();
                                CustomSnackBar.make(coordinatorLayout, "Falha ao realizar login", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                            }
                        });
                    }
                } else {
                    CustomSnackBar.make(coordinatorLayout, "Você está offline", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                }

            }
        });

        singup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(LoginActivity.this, SingupActivity.class), SIGNUP_CODE);
            }
        });

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialog = new PasswordRecoveryDialogFragment();
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        });
    }

    /**
     * Valida os campos do formulário setando mensagens de erro
     */
    private boolean isValidForm() {

        boolean isFocusRequested = false;

        /* Verifica o campo de e-mail*/
        if (!hasValidEmail()) {
            if (!isFocusRequested) {
                etEmail.requestFocus();
                isFocusRequested = true;
            }
        } else {
            formData.put("email", etEmail.getText().toString());
        }

        /* Verifica o campo de senha*/
        if (!hasValidPassword()) {
            if (!isFocusRequested) {
                etPassword.requestFocus();
                isFocusRequested = true;
            }
        } else {
            formData.put("password", etPassword.getText().toString());
        }

        /* Se ninguém pediu foco então tá tudo em ordem */
        return !isFocusRequested;
    }

    private boolean hasValidEmail() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getResources().getString(R.string.err_msg_empty_email));
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getResources().getString(R.string.err_msg_invalid_email));
            return false;
        }

        return true;
    }

    private boolean hasValidPassword() {
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password) || (password.length() < 6) || (password.length() > 22)) {
            etPassword.setError(getResources().getString(R.string.err_msg_short_password));
            return false;
        }
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) { /* Verifica somente quando o foco é perdido */
            switch (v.getId()) {
                case R.id.etEmail:
                    hasValidEmail();
                    break;
                case R.id.etPassword:
                    hasValidPassword();
                    break;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SIGNUP_CODE:
                if (resultCode == SingupActivity.RESULT_OK)
                    CustomSnackBar.make(coordinatorLayout, "Cadastro realizado com sucesso", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.SUCCESS).show();
                break;
        }
    }

    public static class PasswordRecoveryDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_password_recovery, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(view)
                    .setCancelable(false)
                    .setMessage(R.string.dialog_insert_email)
                    .setTitle(R.string.title_password_recovery_dialog)
                    .setPositiveButton(R.string.dialog_action_send, null)
                    .setNegativeButton(R.string.dialog_action_cancel, null);

            // Create the AlertDialog object and return it
           return builder.create();
        }

        @Override
        public void onResume() {
            super.onResume();

            final AlertDialog dialog = (AlertDialog)getDialog();
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText etEmail = (EditText) dialog.findViewById(R.id.etEmail);

                    String email = etEmail.getText().toString().trim();
                    if (TextUtils.isEmpty(email)) {
                        etEmail.setError(getResources().getString(R.string.err_msg_empty_email));
                    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        etEmail.setError(getResources().getString(R.string.err_msg_invalid_email));
                    } else {
                        sendRecoveryEmail(email);
                        dialog.dismiss();
                    }
                }
            });
        }

        private void sendRecoveryEmail(String email) {
            final ProgressBarHelper progressHelper = new ProgressBarHelper(getActivity(), null);

            final ViewGroup coordinatorLayout = getActivity().findViewById(R.id.coordinatorLayout);
            if (NetworkHelper.isOnline(getActivity())) {
                progressHelper.createProgressSpinner();
                HashMap<String, String> formData = new HashMap<>();
                formData.put("email", email);
                NetworkHelper.getInstance(getActivity()).forgotPassword(formData, new ResponseCallback() {
                    @Override
                    public void onSuccess(String jsonStringResponse) {
                        try {
                            progressHelper.dismiss();
                            JSONObject jsonObject = new JSONObject(jsonStringResponse);
                            if (jsonObject.getBoolean("status")) {
                                CustomSnackBar.make(coordinatorLayout, "E-mail de redefinição de senha enviado", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.SUCCESS).show();
                            } else {
                                CustomSnackBar.make(coordinatorLayout, "Falha ao enviar email de redefinição de senha", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFail(VolleyError error) {
                        progressHelper.dismiss();
                        CustomSnackBar.make(coordinatorLayout, "Falha ao enviar email de redefinição de senha", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                    }
                });
            } else {
                CustomSnackBar.make(coordinatorLayout, "Você está offline", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
            }
        }
    }
}
