package com.marco.exifdatamanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.marco.exifdatamanager.enums.ExifTags;
import com.marco.exifdatamanager.resources.GpsData;
import com.marco.utils.MarcoException;

/**
 * This class provides the API to read or update the Exif data of the pictures
 * 
 * @author Marco
 *
 */
public class ExifDataManager {
	private Logger logger = Logger.getLogger(ExifDataManager.class);
	private Path exifToolProgram;

	/**
	 * Provide the path to the exif tool program to call. For example, in Mac is:
	 * /usr/local/bin/exiftool
	 * 
	 * @param pathToExifTool
	 */
	protected ExifDataManager(Path pathToExifTool) {
		this.exifToolProgram = pathToExifTool;
	}

	/**
	 * It returns a map with the values of the tags retrieved from the provided file
	 * 
	 * @param tagsToRead
	 * @param file
	 * @return
	 * @throws MarcoException
	 */
	public Map<ExifTags, String> readExifData(List<ExifTags> tagsToRead, File file) throws MarcoException {
		logger.trace("inside readExifData");

		EnumMap<ExifTags, String> mapOfTags = new EnumMap<>(ExifTags.class);
		for (ExifTags exifTags : tagsToRead) {
			mapOfTags.put(exifTags, null);
		}
		return callExifTool(mapOfTags, false, file);
	}

	/**
	 * It updates the file
	 * 
	 * @param file
	 * @param tagsMap
	 * @return
	 * @throws MarcoException
	 */
	public boolean updateExifDataInFile(File file, Map<ExifTags, String> tagsMap) throws MarcoException {
		callExifTool(tagsMap, true, file);
		return true;
	}

	/**
	 * It converts the GpsData into a map of Exif Tags
	 * 
	 * @param gpsData
	 * @return
	 * @throws MarcoException
	 */
	public Map<ExifTags, String> createGpsExifTags(GpsData gpsData) throws MarcoException {
		logger.trace("inside createGpsExifTags");

		EnumMap<ExifTags, String> tagsToUpdate = new EnumMap<>(ExifTags.class);
		tagsToUpdate.put(ExifTags.GPS_LATITUDE, "" + gpsData.getLat());
		tagsToUpdate.put(ExifTags.GPS_LAT_REF, (gpsData.getLat() < 0 ? "South" : "North"));
		tagsToUpdate.put(ExifTags.GPS_LONGITUTE, "" + gpsData.getLng());
		tagsToUpdate.put(ExifTags.GPS_LONG_REF, "" + (gpsData.getLng() < 0 ? "West" : "East"));

		return tagsToUpdate;
	}

	/**
	 * Given the {@link ExifTags} GPS_LATITUE and GPS_LONGITUDE it returns an
	 * instance of {@link GpsData}, null otherwise
	 * 
	 * @param exifTags
	 * @return
	 */
	public GpsData convertIntoGpsData(Map<ExifTags, String> exifTags) {

		if (exifTags.get(ExifTags.GPS_LATITUDE) != null && exifTags.get(ExifTags.GPS_LONGITUTE) != null) {
			GpsData gpsData = new GpsData();

			double longitude = Double.parseDouble(exifTags.get(ExifTags.GPS_LONGITUTE));
			double latitude = Double.parseDouble(exifTags.get(ExifTags.GPS_LATITUDE));
			gpsData.setLat(latitude);
			gpsData.setLng(longitude);

			return gpsData;

		}
		return null;
	}

	private Map<ExifTags, String> callExifTool(Map<ExifTags, String> tagsValues, boolean updating, File fileToUse)
			throws MarcoException {

		if (tagsValues.size() == 0) {
			throw new MarcoException("Kindly provide the tags to retrieve / update");
		}

		EnumMap<ExifTags, String> map = new EnumMap<>(ExifTags.class);

		try {

			List<String> cmd = new ArrayList<>();
			cmd.add(exifToolProgram.normalize().toString());
			if (!updating) {
				cmd.add("-s");
			}
			for (Map.Entry<ExifTags, String> entry : tagsValues.entrySet()) {
				if (updating) {
					cmd.add("-" + entry.getKey().getExifTag() + "=" + entry.getValue());
				} else {
					cmd.add("-" + entry.getKey().getExifTag());
				}
			}
			if (updating) {
				cmd.add("-overwrite_original");
			}

			cmd.add(fileToUse.getAbsolutePath());

			logger.trace("Command to be executed: " + cmd.toString());
			Process p = Runtime.getRuntime().exec(cmd.toArray(new String[cmd.size()]));
			p.waitFor();

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			String err = null;

			if (updating) {
				while ((line = stdInput.readLine()) != null) {
					logger.trace(line);
				}

				StringBuilder sb = new StringBuilder();
				while ((err = stdError.readLine()) != null) {
					sb.append(err);
				}
				if (sb.length() > 0 && !sb.toString().startsWith("Warning")) {
					throw new MarcoException(sb.toString());
				}

				return null;
			}

			while ((line = stdInput.readLine()) != null) {
				String stringTag = line.substring(0, line.indexOf(":")).trim();
				String valueTag = line.substring(line.indexOf(":") + 1).trim();

				ExifTags tag = ExifTags.getTagFromString(stringTag);
				switch (tag) {
				case GPS_LATITUDE:
					// = Degrees + Minutes/60 + Seconds/3600
					String[] latValues = valueTag.split(" ");
					if (latValues.length == 1) {
						break;
					}
					double latDeg = Double.parseDouble(latValues[0]);
					double latMin = Double.parseDouble(latValues[2].replace("'", ""));
					double latSec = Double.parseDouble(latValues[3].replace("\"", ""));
					double lat = latDeg + latMin / 60 + latSec / 3600;
					lat *= latValues[4].equals("N") ? 1.0 : -1.0;
					map.put(tag, lat + "");
					break;

				case GPS_LONGITUTE:
					String[] lngValues = valueTag.split(" ");
					if (lngValues.length == 1) {
						break;
					}
					double lngDeg = Double.parseDouble(lngValues[0]);
					double lngMin = Double.parseDouble(lngValues[2].replace("'", ""));
					double lngSec = Double.parseDouble(lngValues[3].replace("\"", ""));
					double lng = lngDeg + lngMin / 60 + lngSec / 3600;
					lng *= lngValues[4].equals("E") ? 1.0 : -1.0;
					map.put(tag, lng + "");
					break;
				case DATE_DATE_TIME_ORIGINAL:
				case DATE_CREATE_DATE:
				case DATE_FILE_MODIFIED:
				case DATE_MODIFY_DATE:
					// 2019:01:05 19:07:33
					map.put(tag, valueTag);
					break;
				case MIME_TYPE:
					map.put(tag, valueTag);
					break;
				default:
					logger.error("Tag: " + tag + " not managed");
					throw new MarcoException("Tag: " + tag + " not managed");
				}
			}

		} catch (IOException | InterruptedException e) {
			if (logger.isTraceEnabled()) {
				e.printStackTrace();
			}
			throw new MarcoException(e);
		}

		return map;
	}

}
