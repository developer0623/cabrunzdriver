package com.cabrunzltd.cabrunz.driver.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.cabrunzltd.cabrunz.driver.R;
import com.cabrunzltd.cabrunz.driver.adapter.VehicalTypeListAdapter;
import com.cabrunzltd.cabrunz.driver.base.BaseRegisterFragment;
import com.cabrunzltd.cabrunz.driver.model.VehicalType;
import com.cabrunzltd.cabrunz.driver.parse.AsyncTaskCompleteListener;
import com.cabrunzltd.cabrunz.driver.parse.HttpRequester;
import com.cabrunzltd.cabrunz.driver.parse.ParseContent;
import com.cabrunzltd.cabrunz.driver.utills.AndyConstants;
import com.cabrunzltd.cabrunz.driver.utills.AndyUtils;
import com.cabrunzltd.cabrunz.driver.utills.AppLog;

public class Service_fragment extends BaseRegisterFragment implements AsyncTaskCompleteListener{

	private ListView gvTypes;
	private ArrayList<VehicalType> listType;
	private VehicalTypeListAdapter adapter;
	private ParseContent parseContent;
	private int selectedTypePostion = -1;
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(
				R.layout.serivce_fragment, container, false);
		gvTypes = (ListView) view.findViewById(R.id.listView1);
		return view;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerActivity.actionBar.show();
		registerActivity.setActionBarTitle(getResources().getString(
				R.string.text_register));
		registerActivity.setActionBarIcon(R.drawable.taxi);
		parseContent = new ParseContent(registerActivity);
		
		listType = new ArrayList<VehicalType>();
		adapter = new VehicalTypeListAdapter(registerActivity, listType);
		gvTypes.setAdapter(adapter);
		gvTypes.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				for (int i = 0; i < listType.size(); i++)
					listType.get(i).isSelected = false;
				listType.get(position).isSelected = true;
				// onItemClick(position);
				selectedTypePostion = position;
				adapter.notifyDataSetChanged();
			}
		});
		getVehicalTypes();
	}
	private void getVehicalTypes() {
		AndyUtils.showCustomProgressDialog(registerActivity, "", getResources()
				.getString(R.string.progress_getting_types), false);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(AndyConstants.URL, AndyConstants.ServiceType.GET_VEHICAL_TYPES);
//		AppLog.Log("", AndyConstants.URL);
		new HttpRequester(registerActivity, map,
				AndyConstants.ServiceCode.GET_VEHICAL_TYPES, true, this);
	}
	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		// TODO Auto-generated method stub
//			AppLog.Log("", "Vehicle types  " + response);
			if (parseContent.isSuccess(response)) {
				parseContent.parseTypes(response, listType);
				adapter.notifyDataSetChanged();
			}
			AndyUtils.removeCustomProgressDialog();

	}
}
