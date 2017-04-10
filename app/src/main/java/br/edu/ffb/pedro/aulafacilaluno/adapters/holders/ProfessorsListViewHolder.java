package br.edu.ffb.pedro.aulafacilaluno.adapters.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import br.edu.ffb.pedro.aulafacilaluno.R;

/**
 * Created by Pedro on 03/04/2017.
 */

public class ProfessorsListViewHolder extends RecyclerView.ViewHolder {

    public TextView professorDeviceReadableName;

    public ProfessorsListViewHolder(View itemView) {
        super(itemView);
        professorDeviceReadableName = (TextView) itemView.findViewById(R.id.professorDeviceReadableName);
    }
}
