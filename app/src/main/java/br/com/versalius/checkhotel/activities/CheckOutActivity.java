package br.com.versalius.checkhotel.activities;

import com.google.gson.Gson;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.versalius.checkhotel.R;
import br.com.versalius.checkhotel.network.NetworkHelper;
import br.com.versalius.checkhotel.network.ResponseCallback;
import br.com.versalius.checkhotel.utils.CustomSnackBar;
import br.com.versalius.checkhotel.utils.ProgressDialogHelper;
import br.com.versalius.checkhotel.utils.SessionHelper;

public class CheckOutActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private TextInputLayout tilBookingNumber;
    private TextInputLayout tilItems;
    private TextInputLayout tilItemsOut;

    private TextView tvSpProductsErrMessage;
    private TextView tvSpProductsOutErrMessage;

    private EditText etBookingNumber;
    private EditText etCheckout;
    private EditText etSuggestions;
    private EditText etItems;
    private EditText etItemsOut;

    private Spinner spProducts;
    private Spinner spProductsOut;

    private FloatingActionButton fabAddItems;
    private Button btCheckout;

    private LinearLayout container;
    private CoordinatorLayout coordinatorLayout;

    private Pattern pat;
    private Matcher mat;

    private HashMap<String, String> formData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionHelper.isLogged()) {
            finish();
        } else {
            setContentView(R.layout.activity_check_out);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_checkout);

            formData = new HashMap<>();
            setUpViews();
        }
    }

    public void setUpViews() {

        // Instanciando TextInputLayout
        tilBookingNumber = (TextInputLayout) findViewById(R.id.tilBookingNumber);
        tilItems = (TextInputLayout) findViewById(R.id.tilIems);

        // Instanciando TExtViews
        tvSpProductsErrMessage = (TextView) findViewById(R.id.tvSpProductsErrMessage);

        // Instanciando Campos
        etBookingNumber = (EditText) findViewById(R.id.etBookingNumber);
        etCheckout = (EditText) findViewById(R.id.etCheckOut);
        etSuggestions = (EditText) findViewById(R.id.etSuggestions);
        etItems = (EditText) findViewById(R.id.etItems);

        // Instanciando Botões
        fabAddItems = (FloatingActionButton) findViewById(R.id.fabAddItems);
        btCheckout = (Button) findViewById(R.id.btCheckout);

        // Instanciando Spinners
        spProducts = (Spinner) findViewById(R.id.spItems);

        // Instanciando layout
        container = (LinearLayout) findViewById(R.id.container);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.cl_activity_check_out);

        // Adicionando FocusListener
        etBookingNumber.setOnFocusChangeListener(this);
        etItems.setOnFocusChangeListener(this);

        /* Seta o comportamento do DatePicker */

        final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar nowCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC-3"));
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                etCheckout.setText(dateFormatter.format(newDate.getTime()));
            }

        }, nowCalendar.get(Calendar.YEAR), nowCalendar.get(Calendar.MONTH), nowCalendar.get(Calendar.DAY_OF_MONTH));

        /* Define a data corrente como o dia selecionável */
        datePickerDialog.getDatePicker().setMaxDate(nowCalendar.getTimeInMillis());
        datePickerDialog.getDatePicker().setMinDate(nowCalendar.getTimeInMillis());

        etCheckout.setInputType(InputType.TYPE_NULL);
        etCheckout.setText(dateFormatter.format(nowCalendar.getTime()));

        //Abre o Date Picker com click (só funciona se o campo tiver foco)
        etCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });

        //Abre o Date Picker assim que o campo receber foco
        etCheckout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    datePickerDialog.show();
            }
        });

        // Ação do Add Button
        fabAddItems.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View addView = layoutInflater.inflate(R.layout.row_checkout_content, null);

                tilItemsOut = (TextInputLayout) addView.findViewById(R.id.tilIemsOut);
                tvSpProductsOutErrMessage = (TextView) addView.findViewById(R.id.tvSpProductsOutErrMessage);
                spProductsOut = (Spinner) addView.findViewById(R.id.spItemsOut);
                etItemsOut = (EditText) addView.findViewById(R.id.etItemsOut);

                View.OnFocusChangeListener listener = new View.OnFocusChangeListener()
                {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus)
                    {
                        if (!hasFocus) {
                            switch (v.getId()) {

                                case R.id.etItemsOut:
                                    hasValidItemsOut();
                                    break;
                            }
                        }
                    }
                };

                etItemsOut.setOnFocusChangeListener(listener);

                container.addView(addView);
            }
        });

        //Ação do botão de checkout
        btCheckout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final ProgressDialogHelper progressHelper = new ProgressDialogHelper(CheckOutActivity.this);
                if (NetworkHelper.isOnline(CheckOutActivity.this)) {
                    if (isValidForm()) {
                        Log.v("Checkout", String.valueOf(formData));
                        progressHelper.createProgressSpinner("Aguarde", "Realizando check-out.", true, false);
                        NetworkHelper.getInstance(CheckOutActivity.this).checkedIn(SessionHelper.getUserId(), Integer.parseInt(etBookingNumber.getText().toString()), new ResponseCallback() {
                            @Override
                            public void onSuccess(String jsonStringResponse) {
                                try {
                                    progressHelper.dismiss();
                                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                    if (jsonObject.getBoolean("status")) {
                                        NetworkHelper.getInstance(CheckOutActivity.this).checkedOut(SessionHelper.getUserId(), Integer.parseInt(etBookingNumber.getText().toString()), new ResponseCallback() {
                                            @Override
                                            public void onSuccess(String jsonStringResponse) {
                                                try {
                                                    progressHelper.dismiss();
                                                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                                    if (jsonObject.getBoolean("status")) {
                                                        CustomSnackBar.make(coordinatorLayout, "O Checkout já foi realizado", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                                    } else {
                                                        NetworkHelper.getInstance(CheckOutActivity.this).checkOut(formData, new ResponseCallback() {
                                                            @Override
                                                            public void onSuccess(String jsonStringResponse) {
                                                                try {
                                                                    progressHelper.dismiss();
                                                                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                                                    if (jsonObject.getBoolean("status")) {
                                                                        setResult(RESULT_OK, null);
                                                                        finish();
                                                                    } else {
                                                                        CustomSnackBar.make(coordinatorLayout, "Falha ao realizar check-out", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                                                    }
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }

                                                            @Override
                                                            public void onFail(VolleyError error) {
                                                                progressHelper.dismiss();
                                                                CustomSnackBar.make(coordinatorLayout, "Falha ao realizar check-out", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                                            }
                                                        });
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onFail(VolleyError error) {
                                                progressHelper.dismiss();
                                                CustomSnackBar.make(coordinatorLayout, "Falha ao realizar check-out", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                            }
                                        });
                                    } else {
                                        CustomSnackBar.make(coordinatorLayout, "É necessário fazer check-in primeiro", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFail(VolleyError error) {
                                progressHelper.dismiss();
                                CustomSnackBar.make(coordinatorLayout, "Falha ao realizar check-out", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                            }
                        });
                    } else {

                    }

                } else {
                    CustomSnackBar.make(coordinatorLayout, "Você está offline", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                }

            }

        });
    }


    private boolean isValidForm() {

        // Variável de controle do formulário
        boolean isFocusRequested = false;

        // Variável para controlar quantidade de itens consumidos
        int childCount = container.getChildCount();

        // Arrays de String para manter valores dos produtos consumidos e quantidade
        String [] strItems;
        String [] strQuantity;

        // Verifica se as views foram adicionadas dinamicamente
        if (childCount == 0){
            strItems = new String[1];
            strQuantity = new String[1];
        } else {
            strItems = new String[childCount+1];
            strQuantity = new String[childCount+1];
        }

        // Verifica se o número de reserva foi inserido e se é um número válido
        if (!hasValidBookingNumber()) {
            tilBookingNumber.requestFocus();
            isFocusRequested = true;
        } else
            formData.put("booking_number", etBookingNumber.getText().toString());

        if (spProducts.getSelectedItemPosition() != 0 && !TextUtils.isEmpty(etItems.getText().toString())) {
            strItems[0] = String.valueOf(spProducts.getSelectedItemPosition());
            strQuantity[0] = etItems.getText().toString();
            hasValidSpProducts();
            hasValidItems();
            fabAddItems.setEnabled(true);
            fabAddItems.setAlpha(1f);
        } else if (TextUtils.isEmpty(etItems.getText().toString()) && spProducts.getSelectedItemPosition() != 0){
            tilItems.requestFocus();
            hasValidSpProducts();
            hasValidItems();
            fabAddItems.setEnabled(false);
            fabAddItems.setAlpha(0.5f);
            isFocusRequested = true;
        } else if (!TextUtils.isEmpty(etItems.getText().toString()) && spProducts.getSelectedItemPosition() == 0) {
            hasValidSpProducts();
            hasValidItems();
            isFocusRequested = true;
            fabAddItems.setEnabled(false);
            fabAddItems.setAlpha(0.5f);
        } else {
            tilItems.setErrorEnabled(false);
            tvSpProductsErrMessage.setVisibility(View.GONE);
            fabAddItems.setEnabled(true);
            fabAddItems.setAlpha(1f);
        }

        for (int iCount = 1; iCount <= childCount; iCount++) {

            // Recupera os elementos adicionados pelo Float Button na posicao iCount-1
            View thisChild = container.getChildAt(iCount-1);

            // Pega os elementos da posicao anterior pelo id
            spProductsOut = (Spinner) thisChild.findViewById(R.id.spItemsOut);
            etItemsOut = (EditText) thisChild.findViewById(R.id.etItemsOut);

            if (spProductsOut.getSelectedItemPosition() != 0 && !TextUtils.isEmpty(etItemsOut.getText().toString())) {
                strItems[iCount] = String.valueOf(spProductsOut.getSelectedItemPosition());
                strQuantity[iCount] = etItemsOut.getText().toString();
                hasValidSpProductsOut();
                hasValidItemsOut();
                fabAddItems.setEnabled(true);
                fabAddItems.setAlpha(1f);
            } else if (TextUtils.isEmpty(etItemsOut.getText().toString()) && spProductsOut.getSelectedItemPosition() != 0){
                tilItemsOut.requestFocus();
                hasValidSpProductsOut();
                hasValidItemsOut();
                fabAddItems.setEnabled(false);
                fabAddItems.setAlpha(0.5f);
                isFocusRequested = true;
            } else if (!TextUtils.isEmpty(etItemsOut.getText().toString()) && spProductsOut.getSelectedItemPosition() == 0) {
                hasValidSpProductsOut();
                hasValidItemsOut();
                fabAddItems.setEnabled(false);
                fabAddItems.setAlpha(0.5f);
                isFocusRequested = true;
            } else {
                tilItemsOut.setErrorEnabled(false);
                tvSpProductsOutErrMessage.setVisibility(View.GONE);
                fabAddItems.setEnabled(true);
                fabAddItems.setAlpha(1f);
            }
        }

        //Toast.makeText(getBaseContext(), new Gson().toJson(strItems), Toast.LENGTH_LONG).show();
        //Toast.makeText(getBaseContext(), new Gson().toJson(strQuantity), Toast.LENGTH_LONG).show();

        formData.put("user_id", String.valueOf(SessionHelper.getUserId()));
        formData.put("key", SessionHelper.getUserKey());
        formData.put("product_id", new Gson().toJson(strItems));
        formData.put("quantity", new Gson().toJson(strQuantity));
        formData.put("checkout", etCheckout.getText().toString());
        formData.put("suggestions", etSuggestions.getText().toString());

        return !isFocusRequested;
    }

    private boolean hasValidBookingNumber() {
        String bookingNumber = etBookingNumber.getText().toString().trim();
        pat = Pattern.compile("^[0-9]+$");
        mat = pat.matcher(bookingNumber);

        if (TextUtils.isEmpty(bookingNumber)) {
            tilBookingNumber.setError(getResources().getString(R.string.err_msg_empty_booking_number));
            return false;
        } else if (!mat.find()) {
            tilBookingNumber.setError(getResources().getString(R.string.err_msg_invalid_booking_number));
            return false;
        }

        tilBookingNumber.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidItems() {
        String items = etItems.getText().toString().trim();

        if (TextUtils.isEmpty(items)) {
            tilItems.setError(getResources().getString(R.string.err_msg_invalid_quantity));
            return false;
        }

        tilItems.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidItemsOut() {
        String itemsOut = etItemsOut.getText().toString().trim();

        if (TextUtils.isEmpty(itemsOut)) {
            tilItemsOut.setError(getResources().getString(R.string.err_msg_invalid_quantity));
            return false;
        }

        tilItemsOut.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidSpProducts() {

        if (spProducts.getSelectedItemPosition() == 0) {
            tvSpProductsErrMessage.setVisibility(View.VISIBLE);
            return false;
        }

        tvSpProductsErrMessage.setVisibility(View.GONE);
        return true;
    }

    private boolean hasValidSpProductsOut() {

        if (spProductsOut.getSelectedItemPosition() == 0) {
            tvSpProductsOutErrMessage.setVisibility(View.VISIBLE);
            return false;
        }

        tvSpProductsOutErrMessage.setVisibility(View.GONE);
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            switch (v.getId()) {
                case R.id.etBookingNumber:
                    hasValidBookingNumber();
                    break;

                case R.id.etItems:
                    hasValidItems();
                    break;
            }
        }
    }

    /*
    private void listAllAddView(){

        int childCount = container.getChildCount();

        for(int i=0; i<childCount; i++){
            View thisChild = container.getChildAt(i);

            spProductsOut = (Spinner) thisChild.findViewById(R.id.spItemsOut);
            etItemsOut = (EditText) thisChild.findViewById(R.id.etItemsOut);

            Toast.makeText(getBaseContext(), "Valor Spinner: "+String.valueOf(spProductsOut.getSelectedItemPosition()), Toast.LENGTH_LONG).show();
            Toast.makeText(getBaseContext(), "Valor EditText: "+etItemsOut.getText(), Toast.LENGTH_LONG).show();
        }
    }
    */
}
