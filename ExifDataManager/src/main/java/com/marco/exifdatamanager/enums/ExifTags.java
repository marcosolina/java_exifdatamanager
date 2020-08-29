package com.marco.exifdatamanager.enums;

/**
 * Tags that I use with the Exif Tool
 * 
 * http://owl.phy.queensu.ca/~phil/exiftool/
 * 
 * @author marco
 *
 */
public enum ExifTags {
	MIME_TYPE("MIMEType"), 
	GPS_LATITUDE("GPSLatitude"), 
	GPS_LAT_REF("GPSLatitudeRef"),
	GPS_LONGITUTE("GPSLongitude"), 
	GPS_LONG_REF("GPSLongitudeRef"), 
	DATE_FILE_MODIFIED("FileModifyDate"),
	DATE_DATE_TIME_ORIGINAL("DateTimeOriginal"), 
	DATE_MODIFY_DATE("ModifyDate"), 
	DATE_CREATE_DATE("CreateDate");

	private final String exifTag;

	ExifTags(String exifTag) {
		this.exifTag = exifTag;
	}

	public String getExifTag() {
		return exifTag;
	}

	public static ExifTags getTagFromString(String strinTag) {
		for (ExifTags tag : ExifTags.values()) {
			if (tag.getExifTag().equals(strinTag)) {
				return tag;
			}
		}
		return null;
	}
}
