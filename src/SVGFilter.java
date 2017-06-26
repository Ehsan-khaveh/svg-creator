/** This class is used to define a filter to accept svg files only while
 * opening a new document.
 *
 * @version 1.1 Built in April 3, 2012.
 */

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class SVGFilter extends FileFilter {
	/**
	 * method filter any other file format except ".svg"
	 */
	public boolean accept(File f) {
		if (f.isDirectory())
			return true;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1)
			if (s.substring(i + 1).toLowerCase().equals("svg"))
				return true;
		return false;
	}

	/**
	 * method getDescription to get description for the filter to be displayed
	 */
	public String getDescription() {
		return "Accepts SVG files only.";
	}
}
