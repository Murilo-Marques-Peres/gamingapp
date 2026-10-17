package com.example.mygamingapp;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MeuAdapter extends RecyclerView.Adapter<MeuAdapter.MeuViewHolder> {

    private List<ItemModel> listaItens;
    private DatabaseHelper db;
    private Context context;
    private boolean ehTabelaHoje;

    public MeuAdapter(List<ItemModel> listaItens, Context context, boolean ehTabelaHoje) {
        this.listaItens = listaItens;
        this.context = context;
        this.db = new DatabaseHelper(context);
        this.ehTabelaHoje = ehTabelaHoje;
    }

    @NonNull
    @Override
    public MeuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itens, parent, false);
        return new MeuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeuViewHolder holder, int position) {
        ItemModel item = listaItens.get(position);

        holder.txtNumero.setText((position + 1) + ".");

        configurarListasDosSpinners(holder);

        holder.spHoraIni.setSelection(((ArrayAdapter) holder.spHoraIni.getAdapter()).getPosition(item.getHoraInicio()));
        holder.spMinIni.setSelection(((ArrayAdapter) holder.spMinIni.getAdapter()).getPosition(item.getMinutoInicio()));
        holder.spHoraFim.setSelection(((ArrayAdapter) holder.spHoraFim.getAdapter()).getPosition(item.getHoraFim()));
        holder.spMinFim.setSelection(((ArrayAdapter) holder.spMinFim.getAdapter()).getPosition(item.getMinutoFim()));

        holder.txtDuracaoItem.setText(item.obterDuracaoSessao());

        mudarEstadoDosComponentes(holder, false);

        if (!ehTabelaHoje) {
            holder.btnHabilitarEdicao.setVisibility(View.GONE);
            holder.btnHoraIni.setVisibility(View.GONE);
            holder.btnHoraFim.setVisibility(View.GONE);
        } else {
            holder.btnHabilitarEdicao.setVisibility(View.VISIBLE);
            holder.btnHoraIni.setVisibility(View.VISIBLE);
            holder.btnHoraFim.setVisibility(View.VISIBLE);
            holder.btnHabilitarEdicao.setText("Editar");

            holder.btnHabilitarEdicao.setOnClickListener(v -> {
                int atualPosition = holder.getAdapterPosition();
                if (atualPosition == RecyclerView.NO_POSITION) return;

                if (!holder.spHoraIni.isEnabled()) {
                    mudarEstadoDosComponentes(holder, true);
                    holder.btnHabilitarEdicao.setText("Salvar");
                } else {
                    mudarEstadoDosComponentes(holder, false);
                    holder.btnHabilitarEdicao.setText("Editar");

                    String hIni = holder.spHoraIni.getSelectedItem().toString();
                    String mIni = holder.spMinIni.getSelectedItem().toString();
                    String hFim = holder.spHoraFim.getSelectedItem().toString();
                    String mFim = holder.spMinFim.getSelectedItem().toString();

                    item.setHoraInicio(hIni); item.setMinutoInicio(mIni);
                    item.setHoraFim(hFim); item.setMinutoFim(mFim);

                    db.atualizarHorarioNoBancoHoje(item.getId(), hIni, mIni, hFim, mFim);

                    holder.txtDuracaoItem.setText(item.obterDuracaoSessao());

                    Toast.makeText(v.getContext(), "Salvo com sucesso!", Toast.LENGTH_SHORT).show();

                    if (context instanceof TodayWindow) {
                        ((TodayWindow) context).atualizarContadorHoras();
                    }
                }
            });

            holder.btnHoraIni.setOnClickListener(v -> {
                int atualPosition = holder.getAdapterPosition();
                if (atualPosition == RecyclerView.NO_POSITION) return;

                Calendar agora = Calendar.getInstance();
                String horaAtual = String.format("%02d", agora.get(Calendar.HOUR_OF_DAY));
                String minutoAtual = String.format("%02d", agora.get(Calendar.MINUTE));

                item.setHoraInicio(horaAtual);
                item.setMinutoInicio(minutoAtual);

                db.atualizarHorarioNoBancoHoje(item.getId(), horaAtual, minutoAtual, item.getHoraFim(), item.getMinutoFim());

                holder.spHoraIni.setSelection(((ArrayAdapter) holder.spHoraIni.getAdapter()).getPosition(horaAtual));
                holder.spMinIni.setSelection(((ArrayAdapter) holder.spMinIni.getAdapter()).getPosition(minutoAtual));

                holder.txtDuracaoItem.setText(item.obterDuracaoSessao());

                if (context instanceof TodayWindow) {
                    ((TodayWindow) context).atualizarContadorHoras();
                }
            });

            holder.btnHoraFim.setOnClickListener(v -> {
                int atualPosition = holder.getAdapterPosition();
                if (atualPosition == RecyclerView.NO_POSITION) return;

                Calendar agora = Calendar.getInstance();
                String horaAtual = String.format("%02d", agora.get(Calendar.HOUR_OF_DAY));
                String minutoAtual = String.format("%02d", agora.get(Calendar.MINUTE));

                item.setHoraFim(horaAtual);
                item.setMinutoFim(minutoAtual);

                db.atualizarHorarioNoBancoHoje(item.getId(), item.getHoraInicio(), item.getMinutoInicio(), horaAtual, minutoAtual);

                holder.spHoraFim.setSelection(((ArrayAdapter) holder.spHoraFim.getAdapter()).getPosition(horaAtual));
                holder.spMinFim.setSelection(((ArrayAdapter) holder.spMinFim.getAdapter()).getPosition(minutoAtual));

                holder.txtDuracaoItem.setText(item.obterDuracaoSessao());

                if (context instanceof TodayWindow) {
                    ((TodayWindow) context).atualizarContadorHoras();
                }
            });
        }
    }

    private void mudarEstadoDosComponentes(MeuViewHolder holder, boolean estado) {
        holder.spHoraIni.setEnabled(estado); holder.spMinIni.setEnabled(estado);
        holder.spHoraFim.setEnabled(estado); holder.spMinFim.setEnabled(estado);
        holder.btnHoraIni.setEnabled(estado); holder.btnHoraFim.setEnabled(estado);
    }

    private void configurarListasDosSpinners(MeuViewHolder holder) {
        Context ctx = holder.itemView.getContext();
        List<String> horas = new ArrayList<>();
        for (int i = 0; i < 24; i++) horas.add(String.format("%02d", i));

        List<String> minutos = new ArrayList<>();
        for (int i = 0; i < 60; i++) minutos.add(String.format("%02d", i));

        ArrayAdapter<String> adapterHoras = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, horas);
        // CORRIGIDO: mudado de 'minutes' para 'minutos' para casar com a declaração acima
        ArrayAdapter<String> adapterMinutos = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, minutos);

        holder.spHoraIni.setAdapter(adapterHoras);
        holder.spHoraFim.setAdapter(adapterHoras);
        holder.spMinIni.setAdapter(adapterMinutos);
        holder.spMinFim.setAdapter(adapterMinutos);
    }

    @Override
    public int getItemCount() { return listaItens.size(); }

    public static class MeuViewHolder extends RecyclerView.ViewHolder {
        TextView txtNumero, txtDuracaoItem;
        Spinner spHoraIni, spMinIni, spHoraFim, spMinFim;
        Button btnHabilitarEdicao, btnHoraIni, btnHoraFim;

        public MeuViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNumero = itemView.findViewById(R.id.txtNumero);
            txtDuracaoItem = itemView.findViewById(R.id.txtDuracaoItem);
            spHoraIni = itemView.findViewById(R.id.spHoraIni);
            spMinIni = itemView.findViewById(R.id.spMinIni);
            spHoraFim = itemView.findViewById(R.id.spHoraFim);
            spMinFim = itemView.findViewById(R.id.spMinFim);
            btnHabilitarEdicao = itemView.findViewById(R.id.btnHabilitarEdicao);
            btnHoraIni = itemView.findViewById(R.id.btnHoraIni);
            btnHoraFim = itemView.findViewById(R.id.btnHoraFim);
        }
    }
}