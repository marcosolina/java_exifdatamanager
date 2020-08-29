package com.marco.exifdatamanager;

import java.nio.file.Path;

/**
 * This factory class helps you to get an instance of the Exif Data Manager
 * Class
 * 
 * 
 * @see <a href="https://exiftool.org/">Link to the tool</a>
 * @author marco
 *
 */
public class ExifDataManagerFactory {

	private Path exifToolPath;

	/**
	 * It returns an instance of the data manager
	 * 
	 * @return
	 */
	public ExifDataManager getExifDataManager() {
		return new ExifDataManager(exifToolPath);
	}

	/**
	 * It returns a copy of the path to the Exif tool
	 * 
	 * @return
	 */
	public Path getExifToolPath() {
		return exifToolPath.toFile().toPath();
	}

	/**
	 * Us this method to set the path to the exif tool executable
	 * 
	 * @param pathToExecutable
	 * @return
	 */
	public ExifDataManagerFactory setExifToolPath(Path pathToExecutable) {
		this.exifToolPath = pathToExecutable.toFile().toPath();
		return this;
	}

}
