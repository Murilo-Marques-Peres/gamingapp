
package com.example.mygamingapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TodayWindow extends AppCompatActivity {

    private DatabaseHelper db;
    private MeuAdapter adapter;
    private List<ItemModel> listaItens;

    private Button btnAdicionarMais;
    private Button btnVerOntem;
    private Button btnExcluirItem;

    private RecyclerView recyclerView;
    private TextView txtTotalHorasHoje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_today_window);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.todayWind),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                });

        db = new DatabaseHelper(this);
        db.verificarEResetarViradaDeDia();

        btnAdicionarMais = findViewById(R.id.btnAdicionarMais);
        recyclerView = findViewById(R.id.rvListagemToday);
        txtTotalHorasHoje = findViewById(R.id.txtTotalHorasHoje);

        // O novo botão precisa existir no XML.
        btnExcluirItem = findViewById(R.id.btnExcluirItem);

        listaItens = db.buscarItensHoje();

        adapter = new MeuAdapter(listaItens, this, true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        atualizarContadorHoras();


        btnAdicionarMais.setOnClickListener(v -> {
            long idDoBanco = db.inserirNoBancoHoje();

            if (idDoBanco == -1) {
                Toast.makeText(
                        this,
                        "Não foi possível adicionar o item.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Busca os dados atualizados diretamente do banco.
            listaItens = db.buscarItensHoje();

            // Atualiza a lista exibida pelo RecyclerView.
            adapter.atualizarLista(listaItens);

            if (!listaItens.isEmpty()) {
                recyclerView.scrollToPosition(listaItens.size() - 1);
            }

            atualizarContadorHoras();
        });


        btnExcluirItem.setOnClickListener(v -> excluirItemSelecionado());
    }

    private void excluirItemSelecionado() {
        long idSelecionado = adapter.getIdItemSelecionado();

        if (idSelecionado == -1) {
            Toast.makeText(
                    this,
                    "Selecione um item da lista primeiro.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Excluir registro")
                .setMessage(
                        "Deseja realmente excluir o registro selecionado?")
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    // Não faz nada se o usuário cancelar.
                })
                .setPositiveButton("Excluir", (dialog, which) -> {

                    boolean excluido = db.excluirItemHoje(idSelecionado);

                    if (excluido) {
                        // Busca novamente os registros existentes no SQLite.
                        listaItens = db.buscarItensHoje();

                        // Atualiza os dados apresentados pelo RecyclerView.
                        adapter.atualizarLista(listaItens);

                        // Atualiza o total de horas.
                        atualizarContadorHoras();

                        Toast.makeText(
                                this,
                                "Registro excluído com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {
                        Toast.makeText(
                                this,
                                "O registro não foi encontrado.",
                                Toast.LENGTH_SHORT
                        ).show();

                        listaItens = db.buscarItensHoje();
                        adapter.atualizarLista(listaItens);
                        atualizarContadorHoras();
                    }
                })
                .show();
    }

    public void atualizarContadorHoras() {
        if (db == null || txtTotalHorasHoje == null) {
            return;
        }

        String tempoTotal = db.calcularTempoTotalHoje();

        txtTotalHorasHoje.setText(
                "Tempo Total Jogado Hoje: " + tempoTotal);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (db != null && adapter != null) {
            listaItens = db.buscarItensHoje();
            adapter.atualizarLista(listaItens);
            atualizarContadorHoras();
        }
    }
}
