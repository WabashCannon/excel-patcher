package format;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import utils.Logger;

public class ColumnFormatData {
	private String title;
	private Set<String> finalDependencies;
	private Vector<Specification> specifications = new Vector<Specification>();
	
	public ColumnFormatData(String title){
		this.title = title;
	}
	
	public void addSpecification(Specification spec){
		for ( Specification spec2 : specifications ){
			if ( spec.getType().equals(spec2.getType()) ){
				Logger.logCrash("Specification "+spec.getType()+" declared twice for column"+getTitle());
			}
		}
		specifications.add(spec);
	}
	
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
	public String getTitle(){
		return title;
	}
	
	public String toString(){
		String str = "";
		str += getTitle();
		for ( Specification spec : specifications ){
			str += "\n --- "+spec.toString();
		}
		return str;
	}
	
	public boolean isRequired(){
		Specification requiredSpec = getSpecification("Required");
		if ( requiredSpec == null ){
			return false;
		} else {
			return Boolean.parseBoolean(requiredSpec.getValue());
		}
	}
	
	public int getMaxCharacterCount(){
		Specification maxCharSpec = getSpecification("MaxPossibleCharacters");
		if ( maxCharSpec == null ){
			return 200;
		} else {
			return Integer.parseInt(maxCharSpec.getValue());
		}
	}
	
	public DataType getType(){
		Specification typeSpec = getSpecification("Type");
		if ( typeSpec == null ){
			return null;
		} else {
			return new DataType( typeSpec.getValue() );
		}
	}
	
	public RichTextString getValue(){
		Specification valueSpec = getSpecification("Value");
		if ( valueSpec == null ){
			return null;
		} else {
			return new XSSFRichTextString( valueSpec.getValue() );
		}
	}
	
	public void setFinalDependencies(Set<String> finalDeps){
		finalDependencies = finalDeps;
	}
	
	public Set<String> getFinalDependencies(){
		if ( finalDependencies != null ){
			return finalDependencies;
		}
		Logger.logCrash("Tried to fetch finalized dependencies before they were finalized");
		return null;
	}
	
	// ####################################################
	// ### Private get specifications
	// ####################################################
	private Specification getSpecification(String type){
		if ( !KeywordChecker.containsIgnoresCase(Specification.SPECIFICATION_KEYWORDS, type) ){
			Logger.logCrash( "Tried to get specification of type "+type
					+" which is not a specification keyword");
		}
		for ( Specification spec : specifications){
			if ( spec.getType().equals(type) ){
				return spec;
			}
		}
		//If none found
		return null;
	}
}
