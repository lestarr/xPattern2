package modelparts;

import java.util.List;

/**
 * contains word parts and their titles (WPartTitle) eg: _rechts#populist#en_
 * would have: en_ - with title flex, good (more than 1 split) _rechtspopulist -
 * with title root, good populist with title part, new (only 1 split) _rechts
 * with title part, new
 * 
 * @author halyna.galanzina
 *
 */
public class WordPart {

	private WPartTitle title;
	private String str;

	public WordPart(WPartTitle t, String s) {
		this.title = t;
		this.str = s;
	}

	public String getString() {
		return this.str;
	}

	public WPartTitle getTitle() {
		return this.title;
	}

	public String toString() {
		String good;
		if (this.title.isGood)
			good = "good";
		else
			good = "new";
		String part = "part";
		if (this.title.isFlex)
			part = "flex";
		else if (this.title.isRoot)
			part = "root";
		return this.str + ":" + good + "_" + part;
	}

	public static String getFlexion(List<WordPart> wpList) {
		for (WordPart wp : wpList) {
			if (wp.getTitle().isFlex)
				return wp.str;
		}
		return "_";
	}

	public static String getRoot(List<WordPart> wpList) {
		for (WordPart wp : wpList) {
			if (wp.getTitle().isRoot && wp.getTitle().isGood)
				return wp.str;
		}
		return "";
	}

}
