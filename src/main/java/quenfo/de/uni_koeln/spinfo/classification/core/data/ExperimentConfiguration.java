package quenfo.de.uni_koeln.spinfo.classification.core.data;

import java.io.File;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import quenfo.de.uni_koeln.spinfo.classification.core.classifier.AbstractClassifier;
import quenfo.de.uni_koeln.spinfo.classification.core.feature_engineering.feature_weighting.AbstractFeatureQuantifier;


/**
 * 
 * stores all config-paramters of an experiment including the Classifier, FeatureQuantifer and FeatureUnitConfiguration and Modelfile
 *
 */
public class ExperimentConfiguration {
	
	private FeatureUnitConfiguration fuc;
	private AbstractFeatureQuantifier fq;
	private AbstractClassifier classifier;
	private File dataFile;
	/**
	 * unique model-filename for this experiment 
	 */
	private String modelFileName;
	/**
	 * name of the superior output folder which includes the models, results etc. 
	 */
	private String outputFolder;
	
	/**
	 * @param fuc the featureUnitConfiguration
	 * @param fq the featureQuantifier
	 * @param classifier
	 * @param dataFile
	 * @param outputFolder
	 */
	public ExperimentConfiguration(FeatureUnitConfiguration fuc,
			AbstractFeatureQuantifier fq, AbstractClassifier classifier,
			File dataFile, String outputFolder) {
		super();
		this.fuc = fuc;
		this.fq = fq;
		this.classifier = classifier;
		this.dataFile = dataFile;
		this.outputFolder = outputFolder;
	}

	/**
	 * @return modelFileName
	 */
	public String getModelFileName() {
		if(modelFileName != null){
			File dir = new File(outputFolder+"/myModels");
			if(!dir.exists()){
				dir.mkdirs();
			}
			return outputFolder+"/"+dir.getName()+"/"+modelFileName + ".model";
		}
		File dir =  new File(outputFolder+"/allModels");
		if(!dir.exists()){
			dir.mkdirs();
		}
		return getDefaultModelFileName();
	}
	

	
	private String getDefaultModelFileName(){
		String fqName = "";
		if(this.fq!= null){
			fqName = fq.getClass().getSimpleName();
		}
		String classifierConf = classifier.getClass().getSimpleName();
		String toReturn =  outputFolder+"/allModels/" + 
				fuc.toString()+"&"+classifierConf+"&"+
				fqName+"&"+
				dataFile.getName()+".model";
		return toReturn.replaceAll(":", "");
	}

	/**
	 * sets an individual modelFileName (different from the default model-filename)
	 * @param modelFileName the filename of the model
	 */
	public void setModelFileName(String modelFileName) {
		this.modelFileName = modelFileName;
	}
	

	/**
	 * @return featureUnitConfiguration
	 */
	public FeatureUnitConfiguration getFeatureConfiguration() {
		return fuc;
	}

	/**
	 * @return featureQuantifier
	 */
	public AbstractFeatureQuantifier getFeatureQuantifier() {
		return fq;
	}

	/**
	 * @return classifier
	 */
	public AbstractClassifier getClassifier() {
		return classifier;
	}

	/**
	 * @return dataFile
	 */
	public File getDataFile() {
		return dataFile;
	}

	/**
	 * @return modelFile
	 */
	public File getModelFile() {
		File file = new File(getModelFileName());
		return file;

	}
	
	@Override
	public boolean equals(Object obj) {
		ExperimentConfiguration other = (ExperimentConfiguration) obj;
		
		if (!this.fuc.equals(other.fuc))
			return false;
		if (!this.fq.getClass().equals(other.fq.getClass()))
			return false;
		if (!this.classifier.equals(other.classifier))
			return false;
		if (!this.dataFile.equals(other.dataFile))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(fuc.hashCode());
		builder.append(fq.hashCode());
		builder.append(classifier.hashCode());
		builder.append(dataFile.hashCode());
		builder.append(dataFile.lastModified());
		return builder.hashCode();
	}


	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(fuc.toString());
		if (fq != null) {
			//buff.append("_");
			buff.append(fq.getClass().getSimpleName());
		}
		buff.append("_" + classifier.getClass().getSimpleName());		
		if (classifier.getDistance() != null) {
			buff.append("_");
			buff.append(classifier.getDistance());
		}
		if (classifier.getClassifierConfig() != null) {
			buff.append("_");
			buff.append(classifier.getClassifierConfig());
		}
		buff.append("_");
		
		buff.append(dataFile.getName());
		
		return buff.toString();
	}
	
	public String getOutputFolder(){
		return outputFolder;
	}
}
