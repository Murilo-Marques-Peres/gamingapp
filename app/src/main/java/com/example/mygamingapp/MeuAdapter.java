
package com.example.mygamingapp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MeuAdapter extends RecyclerView.Adapter<MeuAdapter.ViewHolder> {

    private final List<ItemModel> listaItens;
    private final Context context;
    private final boolean ehTabelaHoje;
    private final DatabaseHelper db;

    private long idItemSelecionado = -1;
    private long idItemEmEdicao = -1;

    public MeuAdapter(List<ItemModel> listaItens, Context context,
                      boolean ehTabelaHoje) {
        this.listaItens = new ArrayList<>(listaItens);
        this.context = context;
        this.ehTabelaHoje = ehTabelaHoje;
        this.db = new DatabaseHelper(context);
    }

    public long getIdItemSelecionado() {
        return idItemSelecionado;
    }

    public void limparSelecao() {
        idItemSelecionado = -1;
        notifyDataSetChanged();
    }

    public void atualizarLista(List<ItemModel> novosItens) {
        List<ItemModel> copia = new ArrayList<>(novosItens);

        listaItens.clear();
        listaItens.addAll(copia);

        boolean selecaoExiste = false;
        boolean itemEmEdicaoExiste = false;

        for (ItemModel item : listaItens) {
            if (item.getId() == idItemSelecionado) {
                selecaoExiste = true;
            }

            if (item.getId() == idItemEmEdicao) {
                itemEmEdicaoExiste = true;
            }
        }

        if (!selecaoExiste) {
            idItemSelecionado = -1;
        }

        if (!itemEmEdicaoExiste) {
            idItemEmEdicao = -1;
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itens, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemModel item = listaItens.get(position);
        long idAtual = item.getId();

        holder.txtNumero.setText(String.valueOf(position + 1));
        holder.txtDuracaoItem.setText(item.obterDuracaoSessao());

        // Destaca a linha selecionada para exclusão.
        holder.itemView.setBackgroundColor(
                idAtual == idItemSelecionado
                        ? Color.LTGRAY
                        : Color.TRANSPARENT
        );

        // Opções dos Spinners.
        String[] horas = new String[24];
        for (int i = 0; i < 24; i++) {
            horas[i] = String.format("%02d", i);
        }

        String[] minutos = new String[60];
        for (int i = 0; i < 60; i++) {
            minutos[i] = String.format("%02d", i);
        }

        ArrayAdapter<String> adapterHoras = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                horas
        );
        adapterHoras.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        ArrayAdapter<String> adapterMinutos = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                minutos
        );
        adapterMinutos.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        holder.spHoraIni.setAdapter(adapterHoras);
        holder.spMinIni.setAdapter(adapterMinutos);
        holder.spHoraFim.setAdapter(adapterHoras);
        holder.spMinFim.setAdapter(adapterMinutos);

        selecionarValor(holder.spHoraIni, item.getHoraInicio());
        selecionarValor(holder.spMinIni, item.getMinutoInicio());
        selecionarValor(holder.spHoraFim, item.getHoraFim());
        selecionarValor(holder.spMinFim, item.getMinutoFim());

        // Selecionar uma linha.
        holder.itemView.setOnClickListener(v -> {
            int posicao = holder.getBindingAdapterPosition();

            if (posicao == RecyclerView.NO_POSITION) {
                return;
            }

            idItemSelecionado = listaItens.get(posicao).getId();
            notifyDataSetChanged();
        });

        if (ehTabelaHoje) {
            holder.spHoraIni.setVisibility(View.VISIBLE);
            holder.spMinIni.setVisibility(View.VISIBLE);
            holder.spHoraFim.setVisibility(View.VISIBLE);
            holder.spMinFim.setVisibility(View.VISIBLE);

            holder.btnHabilitarEdicao.setVisibility(View.VISIBLE);
            holder.btnHoraIni.setVisibility(View.VISIBLE);
            holder.btnHoraFim.setVisibility(View.VISIBLE);

            // Só habilita os Spinners da linha que está sendo editada.
            boolean editando = idAtual == idItemEmEdicao;

            holder.spHoraIni.setEnabled(editando);
            holder.spMinIni.setEnabled(editando);
            holder.spHoraFim.setEnabled(editando);
            holder.spMinFim.setEnabled(editando);

            holder.btnHoraIni.setEnabled(editando);
            holder.btnHoraFim.setEnabled(editando);

            holder.btnHabilitarEdicao.setText(
                    editando ? "Salvar" : "Editar"
            );

            // Editar e salvar.
            holder.btnHabilitarEdicao.setOnClickListener(v -> {
                int posicao = holder.getBindingAdapterPosition();

                if (posicao == RecyclerView.NO_POSITION) {
                    return;
                }

                ItemModel itemAtual = listaItens.get(posicao);
                long id = itemAtual.getId();

                // Primeiro clique: habilita os horários.
                if (idItemEmEdicao != id) {
                    idItemEmEdicao = id;
                    notifyDataSetChanged();
                    return;
                }

                // Segundo clique: salva os horários.
                String horaIni =
                        holder.spHoraIni.getSelectedItem().toString();
                String minIni =
                        holder.spMinIni.getSelectedItem().toString();
                String horaFim =
                        holder.spHoraFim.getSelectedItem().toString();
                String minFim =
                        holder.spMinFim.getSelectedItem().toString();

                db.atualizarHorarioNoBancoHoje(
                        id,
                        horaIni,
                        minIni,
                        horaFim,
                        minFim
                );

                idItemEmEdicao = -1;

                Toast.makeText(
                        context,
                        "Horários atualizados!",
                        Toast.LENGTH_SHORT
                ).show();

                recarregarDados();
            });

            // Registrar o horário de início atual.
            holder.btnHoraIni.setOnClickListener(v -> {
                Calendar calendario = Calendar.getInstance();

                String horaAtual = String.format(
                        Locale.getDefault(),
                        "%02d",
                        calendario.get(Calendar.HOUR_OF_DAY)
                );

                String minutoAtual = String.format(
                        Locale.getDefault(),
                        "%02d",
                        calendario.get(Calendar.MINUTE)
                );

                holder.spHoraIni.setSelection(
                        Integer.parseInt(horaAtual)
                );

                holder.spMinIni.setSelection(
                        Integer.parseInt(minutoAtual)
                );
            });

            // Registrar o horário de fim atual.
            holder.btnHoraFim.setOnClickListener(v -> {
                Calendar calendario = Calendar.getInstance();

                String horaAtual = String.format(
                        Locale.getDefault(),
                        "%02d",
                        calendario.get(Calendar.HOUR_OF_DAY)
                );

                String minutoAtual = String.format(
                        Locale.getDefault(),
                        "%02d",
                        calendario.get(Calendar.MINUTE)
                );

                holder.spHoraFim.setSelection(
                        Integer.parseInt(horaAtual)
                );

                holder.spMinFim.setSelection(
                        Integer.parseInt(minutoAtual)
                );
            });

        } else {
            // A lista de ontem é apenas para consulta.
            holder.spHoraIni.setEnabled(false);
            holder.spMinIni.setEnabled(false);
            holder.spHoraFim.setEnabled(false);
            holder.spMinFim.setEnabled(false);

            holder.btnHabilitarEdicao.setVisibility(View.GONE);
            holder.btnHoraIni.setVisibility(View.GONE);
            holder.btnHoraFim.setVisibility(View.GONE);
        }
    }

    private void selecionarValor(Spinner spinner, String valor) {
        if (valor == null || spinner.getAdapter() == null) {
            return;
        }

        ArrayAdapter<?> adapter =
                (ArrayAdapter<?>) spinner.getAdapter();

        for (int i = 0; i < adapter.getCount(); i++) {
            if (valor.equals(adapter.getItem(i))) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void recarregarDados() {
        List<ItemModel> itensAtualizados;

        if (ehTabelaHoje) {
            itensAtualizados = db.buscarItensHoje();
        } else {
            itensAtualizados = db.buscarItensOntem();
        }

        atualizarLista(itensAtualizados);

        if (context instanceof TodayWindow) {
            ((TodayWindow) context).atualizarContadorHoras();
        }
    }

    @Override
    public int getItemCount() {
        return listaItens.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtNumero;
        TextView txtDuracaoItem;

        Spinner spHoraIni;
        Spinner spMinIni;
        Spinner spHoraFim;
        Spinner spMinFim;

        Button btnHabilitarEdicao;
        Button btnHoraIni;
        Button btnHoraFim;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNumero = itemView.findViewById(R.id.txtNumero);
            txtDuracaoItem = itemView.findViewById(R.id.txtDuracaoItem);

            spHoraIni = itemView.findViewById(R.id.spHoraIni);
            spMinIni = itemView.findViewById(R.id.spMinIni);
            spHoraFim = itemView.findViewById(R.id.spHoraFim);
            spMinFim = itemView.findViewById(R.id.spMinFim);

            btnHabilitarEdicao =
                    itemView.findViewById(R.id.btnHabilitarEdicao);
            btnHoraIni = itemView.findViewById(R.id.btnHoraIni);
            btnHoraFim = itemView.findViewById(R.id.btnHoraFim);
        }
    }
}
