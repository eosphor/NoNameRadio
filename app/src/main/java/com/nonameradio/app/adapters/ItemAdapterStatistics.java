package com.nonameradio.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nonameradio.app.data.DataStatistics;
import com.nonameradio.app.R;

public class ItemAdapterStatistics extends ArrayAdapter<DataStatistics> {
	private final Context context;
	private final int resourceId;

	public ItemAdapterStatistics(Context context, int resourceId) {
		super(context, resourceId);
		this.resourceId = resourceId;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DataStatistics aData = getItem(position);

		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(resourceId, null);
		}

		TextView aTextViewTop = v.findViewById(R.id.stats_name);
		TextView aTextViewBottom = v.findViewById(R.id.stats_value);
		if (aTextViewTop != null) {
			aTextViewTop.setText(aData.Name);
		}
		if (aTextViewBottom != null) {
			aTextViewBottom.setText(aData.Value);
		}

		return v;
	}
}
