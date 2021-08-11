package com.vitandreasorino.savent.Enti.GenerateCodeTab;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.vitandreasorino.savent.R;

import java.util.List;

import Model.DB.CodiciComunicazioneTampone;
import Model.Pojo.CodiceComunicazioneTampone;

public class GenerateCodeFragment extends Fragment {

    private ListView codeListView;
    private ProgressBar progressBarDownloading;
    private ProgressBar progressBarCodeGeneration;
    private SwipeRefreshLayout pullToRefresh;
    private SwipeRefreshLayout emptyResultsPullToRefresh;

    private TextView emptyText;

    private RadioButton radioButtonPositive;
    private RadioButton radioButtonNegative;
    private Button buttonGenerateCode;
    private EditText editTextCodeGenerated;
    private ImageView buttonCopyCode;

    private CodeListAdapter adapter;

    private String actualGeneratedCode = null;


    public GenerateCodeFragment(){ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_generate_code, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inflateAll(view);

        //setting the action for the Pull-To-Refresh action
        pullToRefresh.setOnRefreshListener(() -> downloadAllCode());
        emptyResultsPullToRefresh.setOnRefreshListener( () -> downloadAllCode());

        downloadAllCode();

        buttonGenerateCode.setOnClickListener( button -> generateCode());
        buttonCopyCode.setOnClickListener( button -> copyInClipboard());
    }

    /**
     * Method that put the actual generated code into the clipboard
     */
    private void copyInClipboard(){
        if(actualGeneratedCode == null) return;

        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("codice",actualGeneratedCode);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(getContext(),R.string.codeCopiedToClipboard,Toast.LENGTH_SHORT).show();
    }

    /**
     * Method that manage the creation of a new code using the methods inside the class CodiciComuncazioneTamponi.
     * It also manage the visualization of the progress bar during the download of this information.
     */
    private void generateCode(){
        progressBarCodeGeneration.setVisibility(View.VISIBLE);

        CodiciComunicazioneTampone.generateNewCode( (radioButtonPositive.isChecked() ? true : false), codice -> {
            if(codice != null){
                editTextCodeGenerated.setText(codice.getId());
                actualGeneratedCode = codice.getId();

                //adding the new code to the list inside the adapter
                adapter.addToList(codice);
                adapter.notifyDataSetChanged();
            }else Toast.makeText(getContext(),R.string.generationError, Toast.LENGTH_SHORT).show();

            progressBarCodeGeneration.setVisibility(View.INVISIBLE);
        });
    }

    /**
     * Method that manage the download of all the logged-in ente generated code using the methods inside the class CodiciComuncazioneTamponi.
     * It also manage the visualization of the progress bar during the download of this information.
     */
    private void downloadAllCode(){
        progressBarDownloading.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        CodiciComunicazioneTampone.getAllMyGeneratedCode(list -> {
            adapter = new CodeListAdapter(getContext(),list);
            codeListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            progressBarDownloading.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);

            //Disabling the progress bar inside the SwipeRefreshLayout
            pullToRefresh.setRefreshing(false);
            emptyResultsPullToRefresh.setRefreshing(false);
        });
    }

    private void inflateAll(View view){
        codeListView = view.findViewById(R.id.codeListView);
        progressBarDownloading = view.findViewById(R.id.progressBarDownloading);
        progressBarCodeGeneration = view.findViewById(R.id.progressBarCodeGeneration);
        pullToRefresh = view.findViewById(R.id.pullToRefresh);
        emptyResultsPullToRefresh = view.findViewById(R.id.emptyResultsPullToRefresh);
        emptyText = view.findViewById(R.id.emptyText);

        radioButtonPositive = view.findViewById(R.id.radioButtonPositive);
        radioButtonNegative = view.findViewById(R.id.radioButtonNegative);
        buttonGenerateCode = view.findViewById(R.id.buttonGenerateCode);
        editTextCodeGenerated = view.findViewById(R.id.editTextCodeGenerated);
        buttonCopyCode = view.findViewById(R.id.buttonCopyCode);

        codeListView.setEmptyView(emptyResultsPullToRefresh);
    }
}

/**
 * Custom Adapter created to show the codes inside the ListView.
 */
class CodeListAdapter extends BaseAdapter  {

    private List<CodiceComunicazioneTampone> codici=null;
    private Context context=null;



    public CodeListAdapter(Context context, List<CodiceComunicazioneTampone> codici) {
        this.codici=codici;
        this.context=context;
    }

    /**
     * Add the new code to the top of the list.
     *
     * @param codice
     */
    public void addToList(CodiceComunicazioneTampone codice){
        codici.add(0,codice);
    }


    @Override
    public int getCount() {
        return codici.size();
    }

    @Override
    public Object getItem(int position) {
        return codici.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //espandi il layout per ogni riga della lista
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.code_list_row, null);
        }

        CodiceComunicazioneTampone codice = (CodiceComunicazioneTampone) getItem(position);

        //Setting all the cod information into the view
        TextView codeTextView = convertView.findViewById(R.id.codeTextView);
        TextView codeTypeTextView = convertView.findViewById(R.id.codeTypeTextView);
        TextView dataCreationTextView = convertView.findViewById(R.id.dataCreationTextView);
        TextView codeUsedTextView = convertView.findViewById(R.id.codeUsedTextView);
        ImageView buttonCopyCode = convertView.findViewById(R.id.buttonCopyCode);

        codeTextView.setText(codice.getId());
        codeTypeTextView.setText( (codice.getEsitoTampone() ? context.getString(R.string.positiveCodeType) : context.getString(R.string.negativeCodeType)) );
        dataCreationTextView.setText(codice.getNeutralData());
        codeUsedTextView.setText( (codice.getUsato() ? context.getString(R.string.usedCodeStatus) : context.getString(R.string.unusedCodeStatus)) );

        //In case the code is already used, we have to disable the copy button and show the view with a different background color
        if(codice.getUsato()) {
            buttonCopyCode.setEnabled(false);
            convertView.setBackgroundColor(Color.argb(125,211,211,211));
        } else {
            buttonCopyCode.setEnabled(true);
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        //Adding the click listener for the copy button
        buttonCopyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("codice",codice.getId());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context,R.string.codeCopiedToClipboard,Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }
}
