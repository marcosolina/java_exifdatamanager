package com.marco.exifdatamanager.resources;

import java.io.Serializable;

/**
 * Simple POJO class to hold the EXIF info that are used in
 * this Web Project
 * 
 * @author Marco
 *
 */
public class GpsData implements Serializable {
	private static final long serialVersionUID = -941418775738443330L;
	private double lat;
	private double lng;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

}
