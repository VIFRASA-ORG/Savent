package Model.DB;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import Helper.FirebaseStorage.FirestoreHelper;
import Helper.LocalStorage.SQLiteHelper;
import Model.Closures.ClosureResult;



public class TemporaryExposureKeys {

    private static final String TEK_COLLECTION = "TemporaryExposureKeys";


    /**
     * Method used to let generate to the firebase server, a new TEK for the current device.
     * The new TEK is also saved into the TemporaryExposureKeys local SQLite table.
     *
     * @param context the activity context.
     * @param closureRes closure invoked with the new TEK if the task is successful, null otherwise.
     */
    public final static void generateNewTEK(Context context, ClosureResult<String> closureRes){
        HashMap<String, Date> map = new HashMap<String, Date>();
        map.put("generationTime",new Date());

        FirestoreHelper.db.collection(TEK_COLLECTION).add(map).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                //Saving the new tek to the Internal SQLite database
                Calendar c = Calendar.getInstance();
                c.setTime(map.get("generationTime"));

                SQLiteHelper db = new SQLiteHelper(context);
                db.insertNewTek(task.getResult().getId(), c);

                if (closureRes!= null) closureRes.closure(task.getResult().getId());
            }else{
                if (closureRes!= null) closureRes.closure(null);
            }
        });
    }
}
