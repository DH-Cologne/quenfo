package quenfo.de.uni_koeln.spinfo.information_extraction.apps_orm;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import quenfo.de.uni_koeln.spinfo.classification.core.data.DBMode;
import quenfo.de.uni_koeln.spinfo.core.helpers.PropertiesHandler;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.ExtractionUnit;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.IEType;
import quenfo.de.uni_koeln.spinfo.information_extraction.data.MatchedEntity;
import quenfo.de.uni_koeln.spinfo.information_extraction.workflow.ORMExtractor;

public class MatchTools {
	
	static IEType ieType = IEType.TOOL;

	// Pfad zur ORM-Datenbank
	static String dbFilePath;

	// txt-File mit allen bereits validierten Tools
	static File tools;

	// txt-File zur Speicherung der Match-Statistiken
	static File statisticsFile;

	// Anzahl der Paragraphen aus der Input-DB, gegen die gematcht werden soll
	// (-1 = alle)
	static int queryLimit;

	// Falls nicht alle Paragraphen gematcht werden sollen, hier die
	// Startposition angeben
	static int startPos;

	static int fetchSize;

	// true, falls Koordinationen in Informationseinheit aufgelöst werden sollen
	static boolean expandCoordinates;

	static DBMode dbMode;

	public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {

		if (args.length > 0) {
			String configPath = args[1];
			try {
				dbMode = DBMode.valueOf(args[2].toUpperCase());
			} catch (RuntimeException e) { // IllegalArgumentException oder ArrayIndexOutOfBoundsException
				System.out.println("No Database Mode set. Append results to existing DB.\n"
						+ "To choose mode add 'overwrite' or 'append' to command line interface.");
				dbMode = DBMode.APPEND;
			}
			loadProperties(configPath);
		}

		// create connection to jobad sqlite database
				String databaseUrl = "jdbc:sqlite:" + dbFilePath;
				ConnectionSource jobadConnection = new JdbcConnectionSource(databaseUrl);
				
				if (dbMode.equals(DBMode.OVERWRITE)) {
					try {
						TableUtils.clearTable(jobadConnection, ExtractionUnit.class);
					} catch (SQLException e) {
						System.err.println("Noch keine Daten (ExtractionUnits) zum Überschreiben vorhanden.");
					}
					
					try {
						TableUtils.clearTable(jobadConnection, MatchedEntity.class);
					} catch (SQLException e) {
						System.err.println("Noch keine Daten (Extractions) zum Überschreiben vorhanden.");
					}
				}

				long before = System.currentTimeMillis();

				ORMExtractor extractor = new ORMExtractor(jobadConnection, tools, null,
						ieType, expandCoordinates);
				extractor.stringMatch(startPos, queryLimit, fetchSize);

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

		// initialize and load all properties files
		String quenfoData = configFolder.getParent();
		PropertiesHandler.initialize(configFolder);

		// get values from properties files

		dbFilePath = quenfoData + "/sqlite/orm/" + PropertiesHandler.getStringProperty("general", "orm_database");
		
		queryLimit = PropertiesHandler.getIntProperty("matching", "queryLimit");
		startPos = PropertiesHandler.getIntProperty("matching", "startPos");
		fetchSize = PropertiesHandler.getIntProperty("matching", "fetchSize");
		expandCoordinates = PropertiesHandler.getBoolProperty("matching", "expandCoordinates");

		String toolsFolder = quenfoData + "/resources/information_extraction/tools/";
		tools = new File(toolsFolder + PropertiesHandler.getStringProperty("matching", "tools"));
		statisticsFile = new File(
				toolsFolder + PropertiesHandler.getStringProperty("matching", "toolMatchingStats"));
	}

}
