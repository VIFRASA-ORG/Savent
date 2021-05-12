package Model;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * Helper class for the autentication on Firebase.
 */
public class AuthHelper {

    private FirebaseAuth mAuth;

    public AuthHelper() {
        mAuth = FirebaseAuth.getInstance();
    }

    /** Check if the the user is already logged in
     * @return true if the user is already logged in, false otherwise
     */
    public final boolean isLoggedIn(){
        FirebaseUser cU = mAuth.getCurrentUser();
        return cU != null;
    }

    /**
     *
     * @return The id of the logged-in user if there is, an empty string otherwise
     */
    public final String getUserId(){
        return isLoggedIn() ? mAuth.getCurrentUser().getUid() : "";
    }

    /**
     * Function used to create a new user
     *
     * @param email
     * @param psw
     * @param completeListener the listener to manage the success of failure of the new user creation.
     */
    public final void createNewUser(String email, String psw, OnCompleteListener<AuthResult> completeListener){
        mAuth.createUserWithEmailAndPassword(email,psw).addOnCompleteListener(completeListener);
    }

    /** Function used to login.
     *
     * @param email
     * @param psw
     * @param completeListener Listener to manage the success of failure of the login.
     */
    public final void singIn(String email,String psw,OnCompleteListener<AuthResult> completeListener){
        mAuth.signInWithEmailAndPassword(email,psw).addOnCompleteListener(completeListener);
    }

    /**
     *
     * @param newPsw
     * @param onComplete Completition handler to handle the success or the insuccess
     */
    public final void updatePsw(String newPsw, OnCompleteListener<Void> onComplete){
        if(isLoggedIn()){
            mAuth.getCurrentUser().updatePassword(newPsw).addOnCompleteListener(onComplete);
        }
    }

    /**
     *
     * @param newEmail
     * @param onComplete Completition handler to handle the success or the insuccess
     */
    public final void updateEmail(String newEmail, OnCompleteListener<Void> onComplete){
        if(isLoggedIn()){
            mAuth.getCurrentUser().updateEmail(newEmail).addOnCompleteListener(onComplete);
        }
    }

    /**
     *
     * @param email
     * @param onComplete Completition handler to handle the success or the insuccess
     */
    public final void sendPswResetEmail(String email,OnCompleteListener<Void> onComplete){
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(onComplete);
    }

    /**
     * Double autentication for some secutiry sensitive action
     * @param email
     * @param psw
     * @param onComplete Completition handler to handle the success or the insuccess
     */
    public final void reAuthenticate(String email,String psw,OnCompleteListener<Void> onComplete){
        if(isLoggedIn()){
            FirebaseUser currentUser = mAuth.getCurrentUser();
            AuthCredential cred = EmailAuthProvider.getCredential(email,psw);

            currentUser.reauthenticate(cred).addOnCompleteListener(onComplete);
        }
    }

    /**
     * Function used to Log-out
     */
    public final void logOut(){
        mAuth.signOut();
    }
}
