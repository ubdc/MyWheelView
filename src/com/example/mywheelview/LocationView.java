package com.example.mywheelview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.example.mywheelview.WheelView.OnWheelStopListener;

public class LocationView extends LinearLayout {
	private List<Node> list = new ArrayList<Node>();
	private WheelView<Node> provinces;
	private WheelView<Node> cities;
	private WheelView<Node> counties;
	
	public LocationView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public LocationView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LocationView(Context context) {
		this(context, null);
	}
	
	private void init(Context context, AttributeSet attrs) {
		provinces = new WheelView<Node>(context, attrs);
		cities = new WheelView<Node>(context, attrs);
		counties = new WheelView<Node>(context, attrs);
		
		setOrientation(HORIZONTAL);
		
		addView(provinces, 0, LayoutParams.WRAP_CONTENT, 1);
		addView(cities, 0, LayoutParams.WRAP_CONTENT, 1);
		addView(counties, 0, LayoutParams.WRAP_CONTENT, 1);
		
		parseXml();
		initData();
	}
	
	private void addView(View v, int width, int height, int weight) {
		LayoutParams ll = new LayoutParams(width, height, weight);
		addView(v, ll);
	}
	
	private void parseXml() {
		XmlResourceParser xrp = getResources().getXml(R.xml.province_city);
		Node province = null;
		Node city = null;
		Node county = null;
		try {
			while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
				if (xrp.getEventType() == XmlResourceParser.START_TAG) {
					String tagName = xrp.getName();
					if (tagName.equalsIgnoreCase("province")) {
						province = new Node();
						province.id = xrp.getAttributeValue(null, "id");
						province.name = xrp.getAttributeValue(null, "name");
						list.add(province);
					} else if (tagName.equalsIgnoreCase("city")) {
						city = new Node();
						city.id = xrp.getAttributeValue(null, "id");
						city.name = xrp.getAttributeValue(null, "name");
						province.addChild(city);
					} else if (tagName.equalsIgnoreCase("area")) {
						county = new Node();
						county.id = xrp.getAttributeValue(null, "id");
						county.name = xrp.getAttributeValue(null, "name");
						city.addChild(county);
					}
				}
				xrp.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initData() {
		List<Node> pList = null;
		List<Node> cList = null;
		List<Node> coList = null;
		
		pList = list;
		if (pList != null && pList.size() > 0) {
			Node provinceNode = pList.get(0);
			if (provinceNode != null) {
				cList = provinceNode.childs;
				if (cList != null && cList.size() > 0) {
					Node cityNode = cList.get(0);
					if (cityNode != null) {
						coList = cityNode.childs;
					}
				}
			}
		}
		
		provinces.setList(pList);
		cities.setList(cList);
		counties.setList(coList);
		
		provinces.setOnWheelStopListener(new OnWheelStopListener() {

			@Override
			public void onWheelStop(int position) {
				setCities(list.get(position).childs);
			}
		});
		
		cities.setOnWheelStopListener(new OnWheelStopListener() {
			
			@Override
			public void onWheelStop(int position) {
				List<Node> currCities = cities.getList();
				if (currCities.size() > 0) {
					if (position < currCities.size()) {
						counties.setList(currCities.get(position).childs);
					}
				} else {
					counties.setList((List<Node>)null);
				}
			}
		});
	}
	
	private void setCities(List<Node> currCities) {
		if (currCities != null) {
			cities.setList(currCities);
			if (currCities.size() > 0) {
				counties.setList(currCities.get(0).childs);
			} else {
				counties.setList((List<Node>)null);
			}
		} else {
			cities.setList((List<Node>)null);
			counties.setList((List<Node>)null);
		}
	}
	
	private void setCounties(List<Node> currCounties) {
		if (currCounties != null && currCounties.size() > 0) {
			counties.setList(currCounties);
		} else {
			counties.setList((List<Node>)null);
		}
	}
	
	public String getLocation() {
		Node selectedProvince = provinces.getSelectedItem();
		Node selectedCity = cities.getSelectedItem();
		Node selectedCounty = counties.getSelectedItem();
		StringBuilder sb = new StringBuilder();
		if (selectedProvince != null) sb.append(selectedProvince).append(", ");
		if (selectedCity != null) sb.append(selectedCity).append(", ");
		if (selectedCounty != null) sb.append(selectedCounty).append(", ");
		if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	
	public int getProvinceId() {
		Node selectedProvince = provinces.getSelectedItem();
		if (selectedProvince != null && selectedProvince.id != null) {
			return Integer.parseInt(selectedProvince.id);
		} else {
			return 0;
		}
	}
	
	public String getProvinceName() {
		Node selectedProvince = provinces.getSelectedItem();
		if (selectedProvince != null && selectedProvince.name != null) {
			return selectedProvince.name;
		} else {
			return "";
		}
	}
	
	public int getCityId() {
		Node selectedCity = cities.getSelectedItem();
		if (selectedCity != null && selectedCity.id != null) {
			return Integer.parseInt(selectedCity.id);
		} else {
			return 0;
		}
	}

	public String getCityName() {
		Node selectedCity = cities.getSelectedItem();
		if (selectedCity != null && selectedCity.name != null) {
			return selectedCity.name;
		} else {
			return "";
		}
	}
	
	public int getAreaId() {
		Node selectedArea = counties.getSelectedItem();
		if (selectedArea != null && selectedArea.id != null) {
			return Integer.parseInt(selectedArea.id);
		} else {
			return 0;
		}
	}
	
	public String getAreaName() {
		Node selectedArea = counties.getSelectedItem();
		if (selectedArea != null && selectedArea.name != null) {
			return selectedArea.name;
		} else {
			return "";
		}
	}
	
	public void setLocation(String province, String city, String county) {
		Node provinceNode = null;
		Node cityNode = null;
		Node countyNode = null;
		for (int i = 0; i < list.size(); i++) {
			provinceNode = list.get(i);
			if (provinceNode.name.equals(province)) {
				provinces.setSelection(i);
				
				if (provinceNode.childs != null && provinceNode.childs.size() > 0) {
					setCities(provinceNode.childs);
					for (int j = 0; j < provinceNode.childs.size(); j++) {
						cityNode = provinceNode.childs.get(j);
						if (cityNode.name.equals(city)) {
							cities.setSelection(j);
							
							if (cityNode.childs != null && cityNode.childs.size() > 0) {
								setCounties(cityNode.childs);
								for (int k = 0; k < cityNode.childs.size(); k++) {
									countyNode = cityNode.childs.get(k);
									if (countyNode.name.equals(county)) {
										counties.setSelection(k);
										break;
									}
								}
							} else {
								setCounties(null);
							}
							break;
						}
					}
				} else {
					setCities(null);
				}
				break;
			}
		}
	}
	
	public void setLocation(int provinceId, int cityId, int countyId) {
		Node provinceNode = null;
		Node cityNode = null;
		Node countyNode = null;
		for (int i = 0; i < list.size(); i++) {
			provinceNode = list.get(i);
			if (provinceNode.id.equals(Integer.toString(provinceId))) {
				provinces.setSelection(i);
				
				if (provinceNode.childs != null && provinceNode.childs.size() > 0) {
					setCities(provinceNode.childs);
					for (int j = 0; j < provinceNode.childs.size(); j++) {
						cityNode = provinceNode.childs.get(j);
						if (cityNode.id.equals(Integer.toString(cityId))) {
							cities.setSelection(j);
							
							if (cityNode.childs != null && cityNode.childs.size() > 0) {
								setCounties(cityNode.childs);
								for (int k = 0; k < cityNode.childs.size(); k++) {
									countyNode = cityNode.childs.get(k);
									if (countyNode.id.equals(Integer.toString(countyId))) {
										counties.setSelection(k);
										break;
									}
								}
							} else {
								setCounties(null);
							}
							break;
						}
					}
				} else {
					setCities(null);
				}
				break;
			}
		}
	}
	
	public boolean isIdle() {
		return provinces.isIdle() && cities.isIdle() && counties.isIdle();
	}
	
	private class Node {
		String id;
		String name;
		List<Node> childs;
		
		public void addChild(Node node) {
			if (childs == null) childs = new ArrayList<Node>();
			childs.add(node);
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
