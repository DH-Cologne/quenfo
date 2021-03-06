package quenfo.de.uni_koeln.spinfo.classification.core.classifier;

import java.io.File;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import quenfo.de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import quenfo.de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import quenfo.de.uni_koeln.spinfo.classification.core.data.FeatureUnitConfiguration;
import quenfo.de.uni_koeln.spinfo.classification.core.distance.Distance;
import quenfo.de.uni_koeln.spinfo.classification.core.feature_engineering.feature_weighting.AbstractFeatureQuantifier;



/**
 * @author geduldia
 *
 *An abstract class for all classifiers
 *contains basic functionality of a classifier
 *
 */
public abstract class AbstractClassifier {
	
	protected String classifierConfig;
	protected Distance distance;
	
	protected Dao<? extends Model, ?> modelDao;
	

	/**
	 * 
	 * @param cus List of ClassifyUnits
	 * @param fuc FeatureUnitConfiguration
	 * @param fq FeatureQuantifier
	 * @param trainingDataFile 
	 * @return
	 */
	public abstract Model buildModel(List<ClassifyUnit> cus, FeatureUnitConfiguration fuc,AbstractFeatureQuantifier fq, File trainingDataFile);
	
	/**
	 * @param distance measure
	 */
	public  void setDistance(Distance distance){
		this.distance = distance;
	}
	
	/**
	 * @return distance measure
	 */
	public  Distance getDistance(){
		return distance;
	}
	
	/**
	 * @return classifierConfig
	 */
	public String getClassifierConfig(){
		return classifierConfig;
	}
	
	@Override
	public boolean equals(Object obj) {
		AbstractClassifier other = (AbstractClassifier) obj;
		return this.getClassifierConfig().equals(other.getClassifierConfig());
	}
	
	@Override
	public int hashCode() {
		return this.getClassifierConfig().hashCode();
	}
	
	public abstract Dao<? extends Model,?> getModelDao(ConnectionSource connection);
	
	
	public abstract List<? extends Model> getPersistedModels(int configHash);
	

}
