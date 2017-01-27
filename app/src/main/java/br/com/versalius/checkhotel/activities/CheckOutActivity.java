package br.com.versalius.checkhotel.activities;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.versalius.checkhotel.R;
import br.com.versalius.checkhotel.network.NetworkHelper;
import br.com.versalius.checkhotel.utils.CustomSnackBar;

public class CheckOutActivity extends AppCompatActivity implements View.OnFocusChangeListener{

    private TextInputLayout tilBookingNumber;

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
        setContentView(R.layout.activity_check_out);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_checkout);

        formData = new HashMap<>();

        // Instanciando TextInputLayout
        tilBookingNumber = (TextInputLayout) findViewById(R.id.tilBookingNumber);

        // Instanciando Campos
        etBookingNumber = (EditText) findViewById(R.id.etBookingNumber);
        etCheckout = (EditText) findViewById(R.id.etCheckOut);
        etSuggestions = (EditText) findViewById(R.id.etObservations);
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

        // Ação do Add Button
        fabAddItems.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick (View v){

                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View addView = layoutInflater.inflate(R.layout.row_checkout_content, null);

                spProductsOut = (Spinner) addView.findViewById(R.id.spItemsOut);
                etItemsOut = (EditText) addView.findViewById(R.id.etItemsOut);
                container.addView(addView);
                //Toast.makeText(getBaseContext(), String.valueOf(spProductsOut.getSelectedItemPosition()), Toast.LENGTH_LONG).show();

            }
        });

        //Ação do botão de checkout
        btCheckout.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){

                //Toast.makeText(getBaseContext(), String.valueOf(spProducts.getSelectedItemPosition()), Toast.LENGTH_LONG).show();
                //Toast.makeText(getBaseContext(), etItems.getText().toString(), Toast.LENGTH_LONG).show();

                if (NetworkHelper.isOnline(CheckOutActivity.this)) {

                    if (isValidForm()) {

                    } else{

                    }

                } else {
                    CustomSnackBar.make(coordinatorLayout, "Você está offline", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                }

            }

        });
    }

    private boolean isValidForm (){

        // Variável de controle do formulário
        boolean isFocusRequested = false;

        // Variável para controlar quantidade de itens consumidos
        int childCount = container.getChildCount();

        // Verifica se o número de reserva foi inserido e se é um número válido
        if (!hasValidBookingNumber()) {
            tilBookingNumber.requestFocus();
            isFocusRequested = true;
        } else
            formData.put("booking_number", etBookingNumber.getText().toString());

        formData.put("checkout", etCheckout.getText().toString());
        formData.put("suggestions", etSuggestions.getText().toString());

        for(int iCount=0; iCount<childCount; iCount++){

            // Verifica a quantidade de novos itens adicionados pelo Float Button
            View thisChild = container.getChildAt(iCount);

            // Recura todos os elementos adicionados pelo Float Button
            spProductsOut = (Spinner) thisChild.findViewById(R.id.spItemsOut);
            etItemsOut = (EditText) thisChild.findViewById(R.id.etItemsOut);

            if (spProductsOut.getSelectedItemPosition() != 0 && etItemsOut.length() > 0) {
                formData.put("product_id", String.valueOf(spProductsOut.getSelectedItemPosition()));
                formData.put("quantity", etItemsOut.getText().toString());
            }
        }

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

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            switch (v.getId()) {
                case R.id.etBookingNumber:
                    hasValidBookingNumber();
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
