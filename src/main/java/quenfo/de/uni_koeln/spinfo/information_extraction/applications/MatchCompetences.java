package quenfo.de.uni_koeln.spinfo.information_extraction.applications;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import quenfo.de.uni_koeln.spinfo.information_extraction.data.IEType;
import quenfo.de.uni_koeln.spinfo.information_extraction.db_io.IE_DBConnector;
import quenfo.de.uni_koeln.spinfo.information_extraction.workflow.Extractor;

/**
 * @author geduldia
 * 
 *         workflow to match the already validated competences (from competences.txt) against the as class 3 classified paragraphs
 *         
 *         input: as class 3 (= applicants profile) classified paragraphs
 *         output: all matching competences together with their containing sentence

 */
public class MatchCompetences {

	// wird an den Namen der Output-DB angehängt
	static String jahrgang = "2011";

	// Pfad zur Input-DB mit den klassifizierten Paragraphen
	static String pararaphsDB = /* "D:/Daten/sqlite/CorrectableParagraphs.db"; */"C:/sqlite/classification/CorrectableParagraphs_"
			+ jahrgang + ".db"; //

	// Ordner in dem die neue Output-DB angelegt werden soll
	static String outputFolder = /* "D:/Daten/sqlite/"; */"C:/sqlite/matching/competences/";

	// Name der Output-DB
	static String outputDB = "CompetenceMatches_" + jahrgang + ".db";

	// txt-File mit den validierten Kompetenzen
	static File competences = new File("information_extraction/data/competences/competences.txt");

	// txt-File mit allen 'Modifier'-Ausdrücken
	static File modifier = new File("information_extraction/data/competences/modifier.txt");

	// txt-File zur Speicherung der Match-Statistiken
	static File statisticsFile = new File("information_extraction/data/competences/matchingStats.txt");

	// Anzahl der Paragraphen aus der Input-DB, gegen die gematcht werden soll
	// (-1 = alle)
	static int maxCount = 50;

	// Falls nicht alle Paragraphen gematcht werden sollen, hier die
	// Startposition angeben
	static int startPos = 0;
	
	// true, falls Koordinationen  in Informationseinheit aufgelöst werden sollen
	static boolean resolveCoordinations = true;

	public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
		
		// Verbindung mit Input-DB
		Connection inputConnection = null;
		if (!new File(pararaphsDB).exists()) {
			System.out
					.println("Database don't exists " + pararaphsDB + "\nPlease change configuration and start again.");
			System.exit(0);
		} else {
			inputConnection = IE_DBConnector.connect(pararaphsDB);
		}

		// Verbindung mit Output-DB
		if (!new File(outputFolder).exists()) {
			new File(outputFolder).mkdirs();
		}
		Connection outputConnection = IE_DBConnector.connect(outputFolder + outputDB);
		IE_DBConnector.createExtractionOutputTable(outputConnection, IEType.COMPETENCE, false);
		
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

		// starte Matching
		long before = System.currentTimeMillis();
		//erzeugt einen Index auf die Spalte 'ClassTHREE' (falls noch nicht vorhanden)
		IE_DBConnector.createIndex(inputConnection, "ClassifiedParagraphs", "ClassTHREE");
		Extractor extractor = new Extractor(competences, modifier, IEType.COMPETENCE, resolveCoordinations);
		extractor.stringMatch(statisticsFile, inputConnection, outputConnection, maxCount, startPos);
		long after = System.currentTimeMillis();
		double time = (((double) after - before) / 1000) / 60;
		if (time > 60.0) {
			System.out.println("\nfinished matching in " + (time / 60) + " hours");
		} else {
			System.out.println("\nfinished matching in " + time + " minutes");
		}
	}

}
