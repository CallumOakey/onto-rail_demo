package mesea.csv;


import odase.FastConfig;
import odase.KnowledgeBase;
import odase.utils.CommonNamespaces;
import odase.utils.URIPrefix;
import odase.w3onts.Owl;

import java.io.File;
import java.io.IOException;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

import java.util.List;

import mesea.model.TrackMaintenance;

public class CsvReader {

    private final static String CATALOG = "catalog.xml";

    public CsvReader(){

    }

    /*
    Opens the message provided in the input, calls the parse method on it and puts the result in a generated ontology.
    */
    public static void main(String[] args) throws IOException {
        /*
        args = new String[3];
        args[0] = "http://www.mesea.fr/onto-maintenance/data/mockTestData.csv";
        args[1] = "ontologies/main/mockTest.owl";
        args[2] = "http://www.mesea.fr/onto-maintenance/ontologies/main/mockTest.owl";
        */
        //Verify correct number of inputs

        if(args.length != 3){
            System.out.println(Arrays.asList(args));
            System.out.println("Expected arguments: [MessageURI] [OntologyFile] [OntologyURI]");
            return;
        }

        KnowledgeBase kb = new KnowledgeBase(
                CATALOG,"",
                Arrays.asList("http://www.mesea.fr/onto-maintenance/ontologies/main/trackMaintenance.owl",
                        "rdfstore:rdftrie"),
                "rdfstore:rdftrie",
                new FastConfig());

        //Finds the filepath of the message from MessageURI and parses the lines
        CsvReader reader = new CsvReader();
        String messageURI = args[0];
        /*
        String filePath = URLDecoder.decode(kb.getRdfDataStoreURIResolver().resolveUri(messageURI),"utf8");
        List<String> lines = Files.readAllLines(new File(filePath).toPath(), Charset.forName("ISO-8859-1") );
        */
        List<String> lines = Files.readAllLines(new File(messageURI).toPath(), Charset.forName("ISO-8859-1") );
        System.out.println("File found.");
        String messageContentOntology = args[2];
        reader.parse(kb, messageContentOntology, lines);

        //Generates the ontology containing the data
        Owl owl = new Owl(kb);
        Owl.Ontology ontology = owl.assertOntology(messageContentOntology);
        ontology.getImports().add(owl.newOntology("http://www.mesea.fr/onto-maintenance/ontologies/main/trackMaintenance.owl"));
        PrintWriter output = new PrintWriter(args[1], "UTF-8");
        kb.getRdfDataStore(kb.getUpdateableStoreId()).writeAsXMLFast(output, messageContentOntology,
                Arrays.asList(
                        new URIPrefix(CommonNamespaces.OWL, "owl"),
                        new URIPrefix("http://www.mesea.fr/onto-maintenance/ontologies/main/trackMaintenance.owl#", "tm"),
                        new URIPrefix(messageContentOntology+"#", "")
                ));
    }

    public void parse(KnowledgeBase kb, String messageURI, List<String> lines) throws UnsupportedEncodingException{
        TrackMaintenance tm = new TrackMaintenance(kb);

        for(Integer i = 0; i < lines.size(); i++){
            parseLine(tm, lines.get(i), i, messageURI);
        }
    }

    public void parseLine(TrackMaintenance tm, String line, Integer lineNumber, String messageURI){
        String[] values = line.split(",");
        if(lineNumber == 0){
            locations = values;
        }
        else{
            parseValues(tm, values, messageURI);
        }

    }

    String[] locations;

    public void parseValues(TrackMaintenance tm, String[] line, String messageURI){
        TrackMaintenance.Track trackIndiv = tm.assertTrack(messageURI+"#"+line[0]);
        TrackMaintenance.Rail railIndiv = tm.assertRail(messageURI+"#"+line[0]+"_"+line[1]);
        trackIndiv.getComposedOfRail().add(railIndiv);

        TrackMaintenance.Measurement measurementIndiv = tm.assertMeasurement(messageURI+"#"+line[0]+"_"+line[1]+"_"+locations[2]+"_"+line[2]);
        measurementIndiv.setPosition(Float.parseFloat(locations[2]));
        measurementIndiv.setDeviationValue(Float.parseFloat(line[2]));
        railIndiv.getHasMeasurement().add(measurementIndiv);

        for(Integer i = 3; i < line.length; i++){
            TrackMaintenance.Measurement prev = measurementIndiv;
            measurementIndiv = tm.assertMeasurement(messageURI+"#"+line[0]+"_"+line[1]+"_"+locations[i]+"_"+line[i]);
            measurementIndiv.setPosition(Float.parseFloat(locations[i]));
            measurementIndiv.setDeviationValue(Float.parseFloat(line[i]));
            railIndiv.getHasMeasurement().add(measurementIndiv);
            prev.setFollowedByMeasurement(measurementIndiv);
        }

    }

}
