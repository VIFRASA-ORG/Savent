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

import Model.DAO.CodiciComunicazioneTampone;
import Model.POJO.CodiceComunicazioneTampone;

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


    public GenerateCodeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generate_code, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inflateAll(view);

        //Si settano le attività per il l'azione del PuLL-To-Refresh
        pullToRefresh.setOnRefreshListener(() -> downloadAllCode());
        emptyResultsPullToRefresh.setOnRefreshListener(() -> downloadAllCode());

        downloadAllCode();

        buttonGenerateCode.setOnClickListener(button -> generateCode());
        buttonCopyCode.setOnClickListener(button -> copyInClipboard());
    }

    /**
     * Si inserisce il codice effettivamente generato negli appunti
     */
    private void copyInClipboard() {
        if (actualGeneratedCode == null) return;

        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("codice", actualGeneratedCode);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(getContext(), R.string.codeCopiedToClipboard, Toast.LENGTH_SHORT).show();
    }

    /**
     * Viene gestita la creazione di un nuovo codice utilizzando i metodi all'interno della classe CodiciComuncazioneTamponi.
     * Gestisce anche la visualizzazione della barra di avanzamento durante il download di queste informazioni.
     */
    private void generateCode() {
        progressBarCodeGeneration.setVisibility(View.VISIBLE);

        CodiciComunicazioneTampone.generateNewCode((radioButtonPositive.isChecked() ? true : false), codice -> {
            if (codice != null) {
                editTextCodeGenerated.setText(codice.getId());
                actualGeneratedCode = codice.getId();

                //Si aggiunge il nuovo codice alla lista presente nell'ascoltatore
                adapter.addToList(codice);
                adapter.notifyDataSetChanged();
            } else
                Toast.makeText(getContext(), R.string.generationError, Toast.LENGTH_SHORT).show();

            progressBarCodeGeneration.setVisibility(View.INVISIBLE);
        });
    }

    /**
     * Viene gestito il download di tutto il codice generato dall'ente loggato utilizzando i metodi all'interno
     * della classe CodiciComuncazioneTamponi.
     * Gestisce anche la visualizzazione della barra di avanzamento durante il download di queste informazioni.
     **/
    private void downloadAllCode() {
        progressBarDownloading.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        CodiciComunicazioneTampone.getAllMyGeneratedCode(list -> {
            adapter = new CodeListAdapter(getContext(), list);
            codeListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            progressBarDownloading.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);

            //Si disabilitano tutte le progress bar presenti nello SwipeRefreshLayout
            pullToRefresh.setRefreshing(false);
            emptyResultsPullToRefresh.setRefreshing(false);
        });
    }

    /**
     * Si dichiarano e inizializzano tutte le componenti del layout
     * @param view
     */
    private void inflateAll(View view) {
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
 * Viene impostato un adattatore personalizzato creato per mostrare i codici all'interno di ListView.
 */
class CodeListAdapter extends BaseAdapter {

    private List<CodiceComunicazioneTampone> codici = null;
    private Context context = null;

    //Costruttori
    public CodeListAdapter(Context context, List<CodiceComunicazioneTampone> codici) {
        this.codici = codici;
        this.context = context;
    }

    /**
     * Viene aggiunto il nuovo codice in cima alla lista dei codici
     *
     * @param codice generato da aggiungere
     */
    public void addToList(CodiceComunicazioneTampone codice) {
        codici.add(0, codice);
    }

    /**
     * Metodo che restituisce il numero di codici della lista
     *
     * @return il numero di codici presenti nella lista
     */
    @Override
    public int getCount() {
        return codici.size();
    }

    /**
     * Metodo che permette di ottenere l'elemento dati associato alla posizione specificata nel set di dati.
     *
     * @param position : la posizione dell'elemento di cui vogliamo i dati all'interno del set di dati dell'adattatore.
     * @return i codici nella posizione specificata
     */
    @Override
    public Object getItem(int position) {
        return codici.get(position);
    }

    /**
     * Metodo per l'ottenimento dell'id della riga associato alla posizione specificata nell'elenco.
     *
     * @param position :la posizione dell'elemento all'interno del set di dati dell'adattatore di cui vogliamo l'id della riga.
     * @return L'id dell'elemento nella posizione specificata.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Metodo per l'ottenimento di una vista che mostra i dati nella posizione specificata nel set di dati.
     *
     * @param position    :la posizione dell'elemento all'interno del set di dati dell'adattatore dell'elemento di cui vogliamo la visualizzazione.
     * @param convertView : la vecchia vista da riutilizzare, se possibile.
     * @param parent      : il genitore a cui verrà eventualmente associata questa visualizzazione
     * @return convertView : una vista corrispondente ai dati nella posizione specificata.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Si espande il layout per ogni riga della lista
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.code_list_row, null);
        }

        CodiceComunicazioneTampone codice = (CodiceComunicazioneTampone) getItem(position);

        //Si impostano tutte le informazioni relative ai codici nella vista
        TextView codeTextView = convertView.findViewById(R.id.codeTextView);
        TextView codeTypeTextView = convertView.findViewById(R.id.codeTypeTextView);
        TextView dataCreationTextView = convertView.findViewById(R.id.dataCreationTextView);
        TextView codeUsedTextView = convertView.findViewById(R.id.codeUsedTextView);
        ImageView buttonCopyCode = convertView.findViewById(R.id.buttonCopyCode);

        codeTextView.setText(codice.getId());
        codeTypeTextView.setText((codice.getEsitoTampone() ? context.getString(R.string.positiveCodeType) : context.getString(R.string.negativeCodeType)));
        dataCreationTextView.setText(codice.getNeutralData());
        codeUsedTextView.setText((codice.getUsato() ? context.getString(R.string.usedCodeStatus) : context.getString(R.string.unusedCodeStatus)));

        //Nel caso in cui il codice sia stato già usato, si disabilita il button per la copia del codice
        // e si mostra la vista con un differente colore di sfondo
        if (codice.getUsato()) {
            buttonCopyCode.setEnabled(false);
            convertView.setBackgroundColor(Color.argb(125, 211, 211, 211));
        } else {
            buttonCopyCode.setEnabled(true);
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        //Si aggiunge l'evento relativo al click sul button della copia del codice
        buttonCopyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("codice", codice.getId());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, R.string.codeCopiedToClipboard, Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }
}
