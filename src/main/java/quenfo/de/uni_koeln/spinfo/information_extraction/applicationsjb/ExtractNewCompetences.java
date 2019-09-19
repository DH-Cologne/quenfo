package quenfo.de.uni_koeln.spinfo.information_extraction.applicationsjb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import quenfo.de.uni_koeln.spinfo.information_extraction.data.IEType;
import quenfo.de.uni_koeln.spinfo.information_extraction.db_io.IE_DBConnector;
import quenfo.de.uni_koeln.spinfo.information_extraction.workflow.Extractor;

/**
 * @author geduldia
 * 
 *         Workflow to extract new competences
 * 
 *         Input: to class 3 (= applicants profile) classified paragraphs
 * 
 *         Output: extracted competences
 *
 */
public class ExtractNewCompetences {

	// Pfad zur Input-DB mit den klassifizierten Paragraphen
	static String paraInputDB;

	// Output-Ordner
	static String compIEoutputFolder;

	// Name der Output-DB
	static String compIEOutputDB;

	// txt-File mit allen bereits bekannten (validierten) Kompetenzen (die
	// bekannten Kompetenzn helfen beim Auffinden neuer Kompetenzen)
	static File competences;

	// txt-File mit bekannten (typischen) Extraktionsfehlern (würden ansonsten
	// immer wieder vorgeschlagen werden)
	static File noCompetences;

	// txt-File mit den Extraktionspatterns
	static File compPatterns;

	static File modifier;

	// falls nicht alle Paragraphen aus der Input-DB verwendet werden sollen:
	// hier Anzahl der zu lesenden Paragraphen festlegen
	// -1 = alle
	static int maxCount;

	// falls nur eine bestimmte Anzahl gelesen werden soll, hier die startID
	// angeben
	static int startPos;

	// true, falls Koordinationen in Informationseinheit aufgelöst werden sollen
	static boolean expandCoordinates;

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {

		if (args.length > 0) {
			String configPath = args[0];
			loadProperties(configPath);
		}

		// Verbindung zur Input-DB
		Connection inputConnection = null;
		if (!new File(paraInputDB).exists()) {
			System.out.println(
					"Input-DB '" + paraInputDB + "' does not exist\nPlease change configuration and start again.");
			System.exit(0);
		} else {
			inputConnection = IE_DBConnector.connect(paraInputDB);
		}

		// Prüfe ob maxCount und startPos gültige Werte haben
		String query = "SELECT COUNT(*) FROM ClassifiedParagraphs;";
		Statement stmt = inputConnection.createStatement();
		ResultSet countResult = stmt.executeQuery(query);
		int tableSize = countResult.getInt(1);
		stmt.close();
		if (tableSize <= startPos) {
			System.out.println("startPosition (" + startPos + ")is greater than tablesize (" + tableSize + ")");
			System.out.println("please select a new startPosition and try again");
			System.exit(0);
		}
		if (maxCount > tableSize - startPos) {
			maxCount = tableSize - startPos;
		}

		// Verbindung zur Output-DB
		if (!new File(compIEoutputFolder).exists()) {
			new File(compIEoutputFolder).mkdirs();
		}
		Connection outputConnection = null;
		File outputfile = new File(compIEoutputFolder + compIEOutputDB);
		if (!outputfile.exists()) {
			outputfile.createNewFile();
		}
		outputConnection = IE_DBConnector.connect(compIEoutputFolder + compIEOutputDB);

		// Start der Extraktion:
		long before = System.currentTimeMillis();
		// Index für die Spalte 'ClassTHREE' anlegen für schnelleren Zugriff
		IE_DBConnector.createIndex(inputConnection, "ClassifiedParagraphs", "ClassTHREE");
		Extractor extractor = new Extractor(outputConnection, competences, noCompetences, compPatterns, modifier,
				IEType.COMPETENCE, expandCoordinates);
		if (maxCount == -1) {
			maxCount = tableSize;
		}
		extractor.extract(startPos, maxCount, tableSize, inputConnection, outputConnection);
		long after = System.currentTimeMillis();
		Double time = (((double) after - before) / 1000) / 60;
		if (time > 60.0) {
			System.out.println("\nfinished Competence-Extraction in " + (time / 60) + " hours");
		} else {
			System.out.println("\nFinished Competence-Extraction in " + time + " minutes");
		}

	}

	private static void loadProperties(String folderPath) throws IOException {

		File configFolder = new File(folderPath);

		if (!configFolder.exists()) {
			System.err.println("Config Folder " + folderPath + " does not exist."
					+ "\nPlease change configuration and start again.");
			System.exit(0);
		}
		String quenfoData = configFolder.getParent();

		// load general properties (db path etc.)
		Properties generalProps = loadPropertiesFile(configFolder.getAbsolutePath() + "/general.properties");

//		String jahrgang = props.getProperty("jahrgang");
		paraInputDB = quenfoData + "/sqlite/classification/" + generalProps.getProperty("classifiedParagraphs");// + jahrgang + ".db";
		
		/**
		 * 
		 * TODO 18.09.
		 * Config-File variablen anpassen
		 * 
		 * Auch für Matching etc.
		 * 
		 * 
		 */
		
		
		Properties ieProps = loadPropertiesFile(configFolder.getAbsolutePath() + "/informationextraction.properties");
		
		maxCount = Integer.parseInt(ieProps.getProperty("maxCount"));
		startPos = Integer.parseInt(ieProps.getProperty("startPos"));
		expandCoordinates = Boolean.parseBoolean(ieProps.getProperty("expandCoordinates"));
		
		competences = new File(quenfoData + "/information_extraction/data/competences/" + ieProps.getProperty("competences"));
		noCompetences = new File(quenfoData + "/information_extraction/data/competences/" + ieProps.getProperty("noCompetences"));
		modifier = new File(quenfoData + "/information_extraction/data/competences/" + ieProps.getProperty("modifier"));
		compPatterns = new File(quenfoData + "/information_extraction/data/competences/" + ieProps.getProperty("compPatterns"));
		
		compIEoutputFolder = quenfoData + "/sqlite/information_extraction/competences/";
		compIEOutputDB = ieProps.getProperty("compIEOutputDB");
		
	}

	private static Properties loadPropertiesFile(String path) throws IOException {

		File propsFile = new File(path);
		if (!propsFile.exists()) {
			System.err.println(
					"Config File " + path + " does not exist." + "\nPlease change configuration and start again.");
			System.exit(0);
		}

		Properties properties = new Properties();
		InputStream is = new FileInputStream(propsFile);
		properties.load(is);
		return properties;
	}
}
