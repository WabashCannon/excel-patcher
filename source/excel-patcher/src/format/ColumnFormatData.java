package format;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import utils.Logger;

/**
 * This class is a container for the format requirements of a single column
 * loaded from the format file.
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class ColumnFormatData {
	/** The title of the column that this data is for */
	private String title;
	/** Stores the finalized list of column titles on which this data depends */
	private Set<String> finalDependencies;
	/** This format's specifications */
	private Vector<Specification> specifications = new Vector<Specification>();
	
	/**
	 * Creates a new column format data with the given title
	 * 
	 * @param title for the column that this data is for
	 */
	public ColumnFormatData(String title){
		this.title = title;
	}
	
	/**
	 * Adds a specification to this column's format data
	 * 
	 * @param spec specification to add
	 */
	public void addSpecification(Specification spec){
		for ( Specification spec2 : specifications ){
			if ( spec.getType().equals(spec2.getType()) ){
				Logger.log("Error", "Specification "+spec.getType()+" declared twice for column"+getTitle());
			}
		}
		specifications.add(spec);
	}
	
	/**
	 * Returns the set of direct dependencies that this column has,
	 * i.e. only those titles that appear directly in this column's
	 * specifications
	 * 
	 * @return Set of dependency titles
	 */
	public Set<String> getDependencies(){
		Set<String> deps = new HashSet<String>();
		for ( Specification spec : specifications ){
			deps.addAll(spec.getDependencies());
		}
		if ( deps.contains( getTitle() ) ){
			deps.remove(getTitle());
		}
		return deps;
	}
	
	// ####################################################
	// ### Getters and setters
	// ####################################################
	/**
	 * Returns this instance's title
	 * 
	 * @return the format data's title
	 */
	public String getTitle(){
		return title;
	}
	
	/**
	 * Returns a string representation of this format data
	 * 
	 * @return a string representation of this format data
	 */
	@Override
	public String toString(){
		String str = "";
		str += getTitle();
		for ( Specification spec : specifications ){
			str += "\n --- "+spec.toString();
		}
		return str;
	}
	
	/**
	 * Returns if this column format data demands cells be filled with
	 * something. Passes along the "Required" specification setting.
	 * 
	 * @return if this column is required
	 */
	public boolean isRequired(){
		Specification requiredSpec = getSpecification("Required");
		if ( requiredSpec == null ){
			return false;
		} else {
			return Boolean.parseBoolean(requiredSpec.getValue());
		}
	}
	
	/**
	 * Returns the maximum number of characters allowed in this column's cells.
	 * Defaults to 32767 if not specified in the format file - the Excel maximum.
	 * 
	 * @return the maximumn number of characters allowed in this column's cells
	 */
	public int getMaxCharacterCount(){
		Specification maxCharSpec = getSpecification("MaxPossibleCharacters");
		if ( maxCharSpec == null ){
			return 32767;
		} else {
			return Integer.parseInt(maxCharSpec.getValue());
		}
	}
	
	/**
	 * Returns the DataType required for this column's cells. If no DataType
	 * is required, returns null.
	 * 
	 * @return the DataType required for this column's cells
	 */
	public DataType getType(){
		Specification typeSpec = getSpecification("Type");
		if ( typeSpec == null ){
			return null;
		} else {
			return new DataType( typeSpec.getValue() );
		}
	}
	
	/**
	 * Returns the required content of this cell in RichTextString format. Returns
	 * null if a specific content is not specified.
	 * 
	 * @return the required content of this cell in RichTextString format.
	 */
	public RichTextString getValue(){
		Specification valueSpec = getSpecification("Value");
		if ( valueSpec == null ){
			return null;
		} else {
			return new XSSFRichTextString( valueSpec.getValue() );
		}
	}
	
	/**
	 * Sets the finalized dependencies for this format data. This must
	 * be called before getFinalDependencies(). The argument should differ from
	 * the value of getDependencies() by including only the leaves of this
	 * column's dependency tree.
	 * 
	 * @param finalDeps the finalized set of dependencies for this column
	 */
	public void setFinalDependencies(Set<String> finalDeps){
		finalDependencies = finalDeps;
	}
	
	/**
	 * Returns the finalized set of minimal dependencies for this column. Returns null
	 * and logs an error if these dependencies have not been calculated.
	 * 
	 * @return the set of minimal dependencies for this column.
	 */
	public Set<String> getFinalDependencies(){
		if ( finalDependencies != null ){
			return finalDependencies;
		}
		Logger.log("Error", "Tried to fetch finalized dependencies before they were finalized");
		return null;
	}
	
	/**
	 * Returns the specification with keyword matching type if this column has it.
	 * Otherwise returns null
	 * 
	 * @param type String from the set of Specification.SPECIFICATION_KEYWORDS
	 * @return the specification matching the given type
	 */
	private Specification getSpecification(String type){
		// Check if the argument is a valid specification type
		if ( !KeywordChecker.containsIgnoresCase(Specification.SPECIFICATION_KEYWORDS, type) ){
			Logger.log("Error",  "Tried to get specification of type "+type
					+" which is not a specification keyword");
		}
		// Return the specification if it exists
		for ( Specification spec : specifications){
			if ( spec.getType().equals(type) ){
				return spec;
			}
		}
		//If none found
		return null;
	}
}
