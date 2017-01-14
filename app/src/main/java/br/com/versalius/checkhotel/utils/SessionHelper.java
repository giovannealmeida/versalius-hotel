package br.com.versalius.checkhotel.utils;

import android.content.Context;

import br.com.versalius.checkhotel.model.User;

/**
 * Created by jn18 on 13/01/2017.
 */

public class SessionHelper {
    private static SessionHelper sessionHelper;
    private static Context context;

    public SessionHelper(Context context) {
        this.context = context;
    }

    //O contexto é setado logo após o login para salvar o usuário e persiste até o fim da aplicação
    public static synchronized SessionHelper getInstance(Context context) {
        if(sessionHelper == null){
            sessionHelper = new SessionHelper(context);
        }

        return sessionHelper;
    }

    public static boolean isLogged() {
        //Se houver algum CPF salvo, então exsite um operador logado
        return !Preferences.getInstance(context).load(Preferences.USER_CPF).isEmpty();
    }

    public static void logout() {
        Preferences.getInstance(context).clearAll();
    }

    public static String getUserFirstName() {
        return Preferences.getInstance(context).load(Preferences.USER_FIRST_NAME);
    }

    public static long getUserId() {
        return Long.valueOf(Preferences.getInstance(context).load(Preferences.USER_ID));
    }

    public static void saveUser(User user) {
        try {
            Preferences.getInstance(context).save(
                    Preferences.USER_CPF, user.getCpf());
            Preferences.getInstance(context).save(
                    Preferences.USER_PASSWORD, user.getPassword());
            Preferences.getInstance(context).save(
                    Preferences.USER_FIRST_NAME, user.getFirst_name());
            Preferences.getInstance(context).save(
                    Preferences.USER_ID, user.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
